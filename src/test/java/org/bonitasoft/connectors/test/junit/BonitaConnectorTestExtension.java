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
package org.bonitasoft.connectors.test.junit;

import org.bonitasoft.connectors.test.BonitaContainer;
import org.bonitasoft.connectors.test.ConnectorExecutor;
import org.bonitasoft.connectors.test.impl.ConnectorExecutorImpl;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

public class BonitaConnectorTestExtension implements BeforeAllCallback, AfterAllCallback, ParameterResolver {

    private static final BonitaContainer BONITA_CONTAINER = new BonitaContainer();
    
    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        return parameterContext.getParameter().getType().equals(BonitaContainer.class)
                || parameterContext.getParameter().getType().equals(ConnectorExecutor.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        if(parameterContext.getParameter().getType().equals(BonitaContainer.class)) {
            return BONITA_CONTAINER;
        }
        if(parameterContext.getParameter().getType().equals(ConnectorExecutor.class)) {
            return new ConnectorExecutorImpl(BONITA_CONTAINER);
        }
        return null;
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        BONITA_CONTAINER.stop();
    }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        BONITA_CONTAINER.start();
    }

}
