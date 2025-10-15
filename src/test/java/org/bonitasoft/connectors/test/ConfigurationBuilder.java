/*
 * Copyright (C) 2009 - 2025 Bonitasoft S.A.
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

import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.connectors.test.Configuration.Expression;
import org.bonitasoft.connectors.test.Configuration.Output;

public class ConfigurationBuilder {

    private Map<String, Expression> input = new HashMap<>();
    private Map<String, Output> output = new HashMap<>();
    private String definitionId;
    private String definitionVersion;

    public ConfigurationBuilder withConnectorDefinition(String id, String version) {
        this.definitionId = id;
        this.definitionVersion = version;
        return this;
    }
    
    public ConfigurationBuilder addInput(String name, Expression value) {
        if (input.containsKey(name)) {
            throw new IllegalArgumentException(
                    String.format("'%s' input is already present in the configuration.", name));
        }
        input.put(name, value);
        return this;
    }
    
    public ConfigurationBuilder addOutput(String name, Output output) {
        if (this.output.containsKey(name)) {
            throw new IllegalArgumentException(
                    String.format("'%s' output is already present in the configuration.", name));
        }
        this.output.put(name, output);
        return this;
    }

    public Configuration build() {
        return new Configuration(definitionId, definitionVersion, input, output);
    }

}
