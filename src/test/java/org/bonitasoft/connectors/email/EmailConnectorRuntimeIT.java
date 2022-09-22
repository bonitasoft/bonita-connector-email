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
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;

import org.bonitasoft.connectors.test.Configuration.Expression;
import org.bonitasoft.connectors.test.ConnectorExecutor;
import org.bonitasoft.connectors.test.annotation.BonitaConnectorTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.testcontainers.Testcontainers;

import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;

@BonitaConnectorTest
class EmailConnectorRuntimeIT {

    @RegisterExtension
    static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP);

    @BeforeEach
    void exposeHostPorts() {
        Testcontainers.exposeHostPorts(greenMail.getSmtp().getPort());
    }

    @Test
    void test0(ConnectorExecutor executor) throws Exception {
        var hostPort = greenMail.getSmtp().getPort();
        var configuration = executor.newConfigurationBuilder()
                .withConnectorDefinition("email", "1.2.0")
                .addInput(EmailConnector.SMTP_HOST, Expression.stringValue("host.testcontainers.internal"))
                .addInput(EmailConnector.SMTP_PORT, Expression.intValue(hostPort))
                .addInput(EmailConnector.SSL_SUPPORT, Expression.booleanValue(false))
                .addInput(EmailConnector.STARTTLS_SUPPORT, Expression.booleanValue(false))
                .addInput(EmailConnector.FROM, Expression.stringValue("romain.bioteau@bonitasoft.com"))
                .addInput(EmailConnector.TO, Expression.stringValue("receiver@bonitasoft.com"))
                .addInput(EmailConnector.SUBJECT, Expression.stringValue("Testing Subject"))
                .addInput(EmailConnector.MESSAGE, Expression.stringValue("Hello World"))
                .build();

        Map<String, String> result = executor.execute(configuration);

        assertThat(result).isEmpty();

        final MimeMessage[] messages = greenMail.getReceivedMessages();
        assertEquals(1, messages.length);
        final MimeMessage message = messages[0];
        assertThat(message.getFrom()).hasSize(1);
        assertThat(message.getFrom()[0]).hasToString("romain.bioteau@bonitasoft.com");
        assertThat(message.getRecipients(RecipientType.TO)[0]).hasToString("receiver@bonitasoft.com");
        assertThat(message.getSubject()).isEqualTo("Testing Subject");
        assertThat(GreenMailUtil.getBody(message)).isEqualTo("Hello World");
    }

}
