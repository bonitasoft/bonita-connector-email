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
package org.bonitasoft.connectors.email;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.bonitasoft.connectors.test.Configuration.Expression;
import org.bonitasoft.connectors.test.ConnectorExecutor;
import org.bonitasoft.connectors.test.annotation.BonitaConnectorTest;
import org.junit.jupiter.api.Test;

@BonitaConnectorTest
class EmailConnectorRuntimeIT {
    
    @Test
    void test0(ConnectorExecutor executor) throws Exception {
        var configuration = executor.newConfigurationBuilder()
            .withConnectorDefinition("email", "1.2.0")
            // TODO
            .addInput(EmailConnector.SMTP_HOST, Expression.stringValue(""))
            .addInput(EmailConnector.SMTP_PORT, Expression.intValue(1201))
            // 
            .addInput(EmailConnector.SSL_SUPPORT, Expression.booleanValue(true))
            .addInput(EmailConnector.STARTTLS_SUPPORT, Expression.booleanValue(true))
            .addInput(EmailConnector.FROM, Expression.stringValue("romain.bioteau@bonitasoft.com"))
            .addInput(EmailConnector.TO, Expression.stringValue("romain.bioteau@bonitasoft.com"))
            .addInput(EmailConnector.MESSAGE, Expression.stringValue("Hello World"))
            .build();
        
        Map<String, String> result = executor.execute(configuration);
        
        assertThat(result).isEmpty();
    }
   
}
