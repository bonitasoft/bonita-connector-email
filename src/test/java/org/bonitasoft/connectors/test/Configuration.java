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
package org.bonitasoft.connectors.test;

import java.util.Map;

import org.bonitasoft.engine.expression.ExpressionType;

public class Configuration {

    private String definitionId;
    private String definitionVersion;
    private Map<String, Expression> input;
    private Map<String, Output> output;

    public Configuration(String definitionId,
            String definitionVersion,
            Map<String, Expression> input,
            Map<String, Output> output) {
        this.definitionId = definitionId;
        this.definitionVersion = definitionVersion;
        this.input = input;
        this.output = output;
    }

    public String getDefinitionId() {
        return definitionId;
    }

    public String getDefinitionVersion() {
        return definitionVersion;
    }

    public Map<String, Expression> getInput() {
        return input;
    }

    public Map<String, Output> getOutput() {
        return output;
    }
    
    public static class Expression {

        private String content;
        private ExpressionType type;
        private String returnType;

        public static Expression stringValue(String value) {
            return new Expression(value, String.class.getName(), ExpressionType.TYPE_CONSTANT);
        }

        public static Expression intValue(int value) {
            return new Expression(String.valueOf(value), Integer.class.getName(), ExpressionType.TYPE_CONSTANT);
        }
        
        public static Expression booleanValue(boolean value) {
            return new Expression(String.valueOf(value), Boolean.class.getName(), ExpressionType.TYPE_CONSTANT);
        }

        public static Expression groovyScript(String script, String returnType) {
            return new Expression(script, returnType, ExpressionType.TYPE_READ_ONLY_SCRIPT);
        }

        public Expression(String content, String returnType, ExpressionType type) {
            this.content = content;
            this.returnType = returnType;
            this.type = type;
        }

        public String getContent() {
            return content;
        }

        public String getReturnType() {
            return returnType;
        }

        public ExpressionType getType() {
            return type;
        }

    }

    public static class Output {

        private final String name;
        private final String type;

        public static Output create(String name, String type) {
            return new Output(name, type);
        }

        private Output(String name, String type) {
            this.name = name;
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

    }


}
