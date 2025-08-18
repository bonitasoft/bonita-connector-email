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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.security.Security;
import java.util.HashMap;
import java.util.Map;

import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;

import org.bonitasoft.engine.api.APIAccessor;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.connector.ConnectorException;
import org.bonitasoft.engine.connector.ConnectorValidationException;
import org.bonitasoft.engine.connector.EngineExecutionContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mock;

import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.DummySSLSocketFactory;
import com.icegreen.greenmail.util.ServerSetupTest;

class EmailConnectorWithSSLIT {
    
    static {
        Security.setProperty("ssl.SocketFactory.provider", DummySSLSocketFactory.class.getName());
    }
    
    @RegisterExtension
    static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTPS);
    

    @Mock
    private EngineExecutionContext engineExecutionContext;
    @Mock
    private APIAccessor apiAccessor;
    @Mock
    private ProcessAPI processAPI;

    private static final String ADDRESSJOHN = "john.doe@bonita.org";
    private static final String SUBJECT = "Testing EmailConnector";


    @Test
    void should_fails_when_checking_server_identity() throws Exception {
        ConnectorException excpetion = assertThrows(ConnectorException.class, () ->  executeConnector(getBasicSSLSettings()));
        assertThat(excpetion.getCause().getCause()).isInstanceOf(IOException.class);
        assertThat(excpetion.getCause().getCause().getMessage()).isEqualTo("Can't verify identity of server: 127.0.0.1");
    }
 
    @Test
    void should_ssl_not_check_server_identity_when_autotrust_is_true() throws Exception {
        Map<String, Object> basicSSLSettings = getBasicSSLSettings();
        basicSSLSettings.put(EmailConnector.TRUST_CERTIFICATE, false);
        executeConnector(basicSSLSettings);
        
        final MimeMessage[] messages = greenMail.getReceivedMessages();
        assertEquals(1, messages.length);
        final MimeMessage message = messages[0];
        assertThat(message.getFrom()).hasSize(1);
        assertThat(message.getRecipients(RecipientType.TO)[0]).hasToString(ADDRESSJOHN);
        assertThat(message.getSubject()).isEqualTo(SUBJECT);
    }
    
    private Map<String, Object> getBasicSSLSettings() {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put(EmailConnector.SMTP_HOST, greenMail.getSmtps().getBindTo());
        parameters.put(EmailConnector.SMTP_PORT, greenMail.getSmtps().getPort());
        parameters.put(EmailConnector.TO, ADDRESSJOHN);
        parameters.put(EmailConnector.SUBJECT, SUBJECT);
        parameters.put(EmailConnector.SSL_SUPPORT, true);
        parameters.put(EmailConnector.HTML, false);
        return parameters;
    }

    private Map<String, Object> executeConnector(final Map<String, Object> parameters)
            throws ConnectorValidationException, ConnectorException {
        final EmailConnector email = new EmailConnector();
        email.setExecutionContext(engineExecutionContext);
        email.setAPIAccessor(apiAccessor);
        email.setInputParameters(parameters);
        email.validateInputParameters();
        return email.execute();
    }

}
