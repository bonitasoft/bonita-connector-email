/*
 * Copyright (C) 2009 - 2020 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.bonitasoft.connectors.test.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import org.apache.maven.cli.MavenCli;
import org.bonitasoft.connectors.test.BonitaContainer;
import org.bonitasoft.connectors.test.Configuration;
import org.bonitasoft.connectors.test.ConfigurationBuilder;
import org.bonitasoft.connectors.test.ConnectorExecutor;
import org.bonitasoft.engine.bpm.bar.BarResource;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.bar.actorMapping.Actor;
import org.bonitasoft.engine.bpm.bar.actorMapping.ActorMapping;
import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.expression.InvalidExpressionException;
import org.bonitasoft.engine.operation.OperationBuilder;

public class ConnectorExecutorImpl implements ConnectorExecutor {

    private BonitaContainer container;

    public ConnectorExecutorImpl(BonitaContainer container) {
        this.container = container;
    }

    @Override
    public ConfigurationBuilder newConfigurationBuilder() {
        return new ConfigurationBuilder();
    }

    @Override
    public Map<String, String> execute(Configuration configuration) throws Exception {
        var projectInfo = readProjectInfo();
        var process = buildConnectorInProcess(configuration);
        var bar = buildBusinessArchive(process, configuration, projectInfo);
        container.installProcess(bar);
        long caseId = container.startProcess(bar.getProcessDefinition().getName());

        // Wait until the process launched is started (and not failed)
        await().until(container.pollStateOf(caseId), "started"::equals);

        var result = new HashMap<String, String>();
        configuration.getOutput().forEach((key, value) -> {
            result.put(key, container.getProcessVariableValue(caseId, key));
        });

        return Collections.unmodifiableMap(result);
    }

    private ProjectInfo readProjectInfo() throws IOException {
        return new ProjectInfo(evaluate("project.build.finalName") + ".jar", resolveRuntimeDependencies());
    }

    private String evaluate(String expression) throws IOException {
        var workingDirectory = new File("").getAbsolutePath();
        var pomFile = new File("").getAbsoluteFile().toPath().resolve("pom.xml");
        var outputFile = Files.createTempFile("evaluate", ".txt");
        try {
            var mavenCli = new MavenCli();
            System.setProperty("maven.multiModuleProjectDirectory", workingDirectory);
            mavenCli.doMain(
                    new String[] {
                            "-f",
                            pomFile.toFile().getAbsolutePath(),
                            "help:evaluate",
                            "-Dexpression=" + expression,
                            "-Doutput=" + outputFile },
                    workingDirectory,
                    System.out,
                    System.err);

            return Files.readString(outputFile);
        } finally {
            Files.delete(outputFile);
        }
    }

    private List<Path> resolveRuntimeDependencies() throws IOException {
        Path pomFile = new File("").getAbsoluteFile().toPath().resolve("pom.xml");
        assertThat(pomFile).exists();
        var outputFile = Files.createTempFile("deps", ".txt");
        var workingDirectory = new File("").getAbsolutePath();
        var mavenCli = new MavenCli();
        System.setProperty("maven.multiModuleProjectDirectory", workingDirectory);
        try {
            mavenCli.doMain(
                    new String[] {
                            "-f",
                            pomFile.toFile().getAbsolutePath(),
                            "dependency:list",
                            "-DincludeScope=runtime",
                            "-DoutputAbsoluteArtifactFilename=true",
                            "-Dmdep.outputScope=false",
                            "-DoutputFile=" + outputFile },
                    workingDirectory,
                    System.out,
                    System.err);

            return Files.readAllLines(outputFile).stream()
                    .skip(2) // Skip file header
                    .filter(line -> !line.isBlank())
                    .map(line -> {
                        var table = line.split(":");
                        var path = table[4];
                        if (table.length > 5) {
                            //windows case
                            path = path + ":" + table[5];
                        }
                        return path;
                    })
                    .map(absolutePath -> Paths.get(absolutePath))
                    .collect(Collectors.toList());
        } finally {
            Files.delete(outputFile);
        }
    }

    private DesignProcessDefinition buildConnectorInProcess(Configuration configuration) throws Exception {
        var processBuilder = new ProcessDefinitionBuilder();
        processBuilder.createNewInstance("PROCESS_UNDER_TEST", "1.0");
        processBuilder.addActor("system");
        var expConverter = expressionConverter();
        var connectorBuilder = processBuilder.addConnector("connector-under-test",
                configuration.getDefinitionId(),
                configuration.getDefinitionVersion(),
                ConnectorEvent.ON_ENTER);
        configuration.getInput()
                .forEach((name, expression) -> connectorBuilder.addInput(name, expConverter.apply(expression)));

        configuration.getOutput().forEach((name, output) -> {
            try {
                processBuilder.addData(name, output.getType(), null);
                connectorBuilder.addOutput(new OperationBuilder().createSetDataOperation(name,
                        new ExpressionBuilder().createDataExpression(output.getName(), output.getType())));
            } catch (InvalidExpressionException e) {
                throw new RuntimeException(e);
            }
        });

        // Add a user task to avoid the process to be already completed as soon as it's launched. 
        processBuilder.addUserTask("waiting task", "system");

        return processBuilder.done();
    }

    private static Function<Configuration.Expression, Expression> expressionConverter() {
        return exp -> {
            var expBuilder = new ExpressionBuilder();
            try {
                return expBuilder.createExpression(UUID.randomUUID().toString(),
                        exp.getContent(),
                        exp.getReturnType(),
                        exp.getType());
            } catch (InvalidExpressionException e) {
                throw new RuntimeException(e);
            }
        };
    }

    private BusinessArchive buildBusinessArchive(DesignProcessDefinition process,
            Configuration configuration,
            ProjectInfo info) throws Exception {
        var barBuilder = new BusinessArchiveBuilder();
        barBuilder.createNewBusinessArchive();
        barBuilder.setProcessDefinition(process);
        var connectorJar = new File("").getAbsoluteFile().toPath()
                .resolve("target")
                .resolve(info.getFinalName())
                .toFile();
        assertThat(connectorJar).exists();
        var connectorDescriptorFilename = configuration.getDefinitionId() + ".impl";
        List<JarEntry> jarEntries = findJarEntries(connectorJar,
                entry -> entry.getName().equals(connectorDescriptorFilename));
        assertThat(jarEntries).hasSize(1);
        var implEntry = jarEntries.get(0);

        byte[] content = null;
        try (JarFile jarFile = new JarFile(connectorJar)) {
            InputStream inputStream = jarFile.getInputStream(implEntry);
            content = inputStream.readAllBytes();
        }

        barBuilder.addConnectorImplementation(
                new BarResource(connectorDescriptorFilename, content));
        barBuilder.addClasspathResource(
                new BarResource(connectorJar.getName(), Files.readAllBytes(connectorJar.toPath())));
        for (Path jarPath : info.getDependencies()) {
            barBuilder.addClasspathResource(
                    new BarResource(jarPath.getFileName().toString(), Files.readAllBytes(jarPath)));
        }

        ActorMapping actorMapping = new ActorMapping();
        var systemActor = new Actor("system");
        systemActor.addUser(container.getDefaultUser().getUserName());
        actorMapping.addActor(systemActor);
        barBuilder.setActorMapping(actorMapping);

        return barBuilder.done();
    }

    private static List<JarEntry> findJarEntries(File file, Predicate<? super JarEntry> entryPredicate)
            throws IOException {
        try (JarFile jarFile = new JarFile(file)) {
            return jarFile.stream()
                    .filter(entryPredicate)
                    .collect(Collectors.toList());
        }
    }

    class ProjectInfo {

        private final List<Path> dependencies;
        private String finalName;

        public ProjectInfo(String finalName, List<Path> dependencies) {
            this.finalName = finalName;
            this.dependencies = dependencies;
        }

        public String getFinalName() {
            return finalName;
        }

        public List<Path> getDependencies() {
            return dependencies;
        }

    }
}
