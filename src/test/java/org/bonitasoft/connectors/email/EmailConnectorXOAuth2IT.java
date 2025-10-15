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
package org.bonitasoft.connectors.email;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import javax.mail.Session;

import org.bonitasoft.engine.api.APIAccessor;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.connector.ConnectorValidationException;
import org.bonitasoft.engine.connector.EngineExecutionContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Integration tests for XOAUTH2 authentication.
 * <p>
 * Note: GreenMail does not support XOAUTH2 SASL authentication, so these tests
 * verify configuration rather than end-to-end email sending.
 * <p>
 * For manual testing with real OAuth2 providers (Office 365, Gmail), see:
 * {@link #testManualXOAuth2WithOffice365()}
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EmailConnectorXOAuth2IT {

    @Mock
    private EngineExecutionContext engineExecutionContext;
    @Mock
    private APIAccessor apiAccessor;
    @Mock
    private ProcessAPI processAPI;

    private static final String ADDRESSJOHN = "john.doe@bonita.org";
    private static final String SUBJECT = "Testing XOAUTH2";

    @BeforeEach
    void setUp() {
        when(apiAccessor.getProcessAPI()).thenReturn(processAPI);
    }

    @Test
    void should_configure_session_with_xoauth2_properties() throws ConnectorValidationException {
        // Given
        Map<String, Object> parameters = getXOAuth2Settings();

        // When
        EmailConnector connector = new EmailConnector();
        connector.setExecutionContext(engineExecutionContext);
        connector.setAPIAccessor(apiAccessor);
        connector.setInputParameters(parameters);
        connector.validateInputParameters();

        Session session = connector.getSession();

        // Then - Verify XOAUTH2 properties are set correctly
        assertThat(session.getProperties().getProperty("mail.smtp.auth")).isEqualTo("true");
        assertThat(session.getProperties().getProperty("mail.smtp.auth.mechanisms")).isEqualTo("XOAUTH2");
        assertThat(session.getProperties().getProperty("mail.smtp.sasl.enable")).isEqualTo("true");
        assertThat(session.getProperties().getProperty("mail.smtp.sasl.mechanisms")).isEqualTo("XOAUTH2");
        // Note: The OAuth2 token is passed via Authenticator, not as a property
        assertThat(session.getProperties().getProperty("mail.smtp.host")).isEqualTo("smtp.office365.com");
        assertThat(session.getProperties().getProperty("mail.smtp.port")).isEqualTo("587");
        assertThat(session.getProperties().getProperty("mail.smtp.starttls.enable")).isEqualTo("true");
    }

    @Test
    void should_not_set_xoauth2_when_disabled() throws ConnectorValidationException {
        // Given
        Map<String, Object> parameters = getBasicSettings();
        parameters.put(EmailConnector.AUTH_TYPE, EmailConnector.BASIC_AUTH_TYPE);
        parameters.put(EmailConnector.USER_NAME, "user@example.com");
        parameters.put(EmailConnector.PASSWORD, "password");

        // When
        EmailConnector connector = new EmailConnector();
        connector.setExecutionContext(engineExecutionContext);
        connector.setAPIAccessor(apiAccessor);
        connector.setInputParameters(parameters);
        connector.validateInputParameters();

        Session session = connector.getSession();

        // Then - Verify XOAUTH2 is NOT configured
        assertThat(session.getProperties().getProperty("mail.smtp.sasl.enable")).isNull();
        assertThat(session.getProperties().getProperty("mail.smtp.sasl.mechanisms")).isNull();
    }

    /**
     * Manual test for Office 365 XOAUTH2.
     * <p>
     * To run this test:
     * 1. Obtain a valid OAuth2 access token from Azure AD with SMTP.Send scope
     * 2. Set environment variables:
     * - SMTP_OAUTH2_USER: Your Office 365 email address
     * - SMTP_OAUTH2_TOKEN: Your OAuth2 access token
     * - SMTP_OAUTH2_TO: Recipient email address
     * 3. Remove @Disabled annotation
     * 4. Run the test
     * <p>
     * Note: Access tokens typically expire after 60-90 minutes.
     * <p>
     * To get a token for testing, you can use Azure CLI:
     * az account get-access-token --resource https://outlook.office.com
     */
    @Test
    @Disabled("Manual test - requires real OAuth2 token from Office 365")
    void testManualXOAuth2WithOffice365() throws Exception {
        // Configuration from environment variables
        String userEmail = System.getenv("SMTP_OAUTH2_USER");
        String accessToken = System.getenv("SMTP_OAUTH2_TOKEN");
        String recipientEmail = System.getenv("SMTP_OAUTH2_TO");

        assertThat(userEmail).as("SMTP_OAUTH2_USER environment variable must be set").isNotNull();
        assertThat(accessToken).as("SMTP_OAUTH2_TOKEN environment variable must be set").isNotNull();
        assertThat(recipientEmail).as("SMTP_OAUTH2_TO environment variable must be set").isNotNull();

        // Setup connector with OAuth2
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(EmailConnector.SMTP_HOST, "smtp.office365.com");
        setConnectorParameters(parameters, userEmail, accessToken, recipientEmail);

        // Execute connector
        EmailConnector connector = new EmailConnector();
        connector.setExecutionContext(engineExecutionContext);
        connector.setAPIAccessor(apiAccessor);
        connector.setInputParameters(parameters);
        connector.validateInputParameters();
        connector.execute();

        // If we get here without exception, the email was sent successfully
        System.out.println("Email sent successfully using XOAUTH2 authentication!");
    }

    /**
     * Manual test for Gmail XOAUTH2.
     * <p>
     * To run this test:
     * 1. Create OAuth2 credentials in Google Cloud Console
     * 2. Obtain a valid OAuth2 access token with https://mail.google.com/ scope
     * 3. Set environment variables:
     * - GMAIL_OAUTH2_USER: Your Gmail address
     * - GMAIL_OAUTH2_TOKEN: Your OAuth2 access token
     * - GMAIL_OAUTH2_TO: Recipient email address
     * 4. Remove @Disabled annotation
     * 5. Run the test
     */
    @Test
    @Disabled("Manual test - requires real OAuth2 token from Gmail")
    void testManualXOAuth2WithGmail() throws Exception {
        // Configuration from environment variables
        String userEmail = System.getenv("GMAIL_OAUTH2_USER");
        String accessToken = System.getenv("GMAIL_OAUTH2_TOKEN");
        String recipientEmail = System.getenv("GMAIL_OAUTH2_TO");

        assertThat(userEmail).as("GMAIL_OAUTH2_USER environment variable must be set").isNotNull();
        assertThat(accessToken).as("GMAIL_OAUTH2_TOKEN environment variable must be set").isNotNull();
        assertThat(recipientEmail).as("GMAIL_OAUTH2_TO environment variable must be set").isNotNull();

        // Setup connector with OAuth2
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(EmailConnector.SMTP_HOST, "smtp.gmail.com");
        setConnectorParameters(parameters, userEmail, accessToken, recipientEmail);

        // Execute connector
        EmailConnector connector = new EmailConnector();
        connector.setExecutionContext(engineExecutionContext);
        connector.setAPIAccessor(apiAccessor);
        connector.setInputParameters(parameters);
        connector.validateInputParameters();
        connector.execute();

        // If we get here without exception, the email was sent successfully
        System.out.println("Email sent successfully using XOAUTH2 authentication with Gmail!");
    }

    private Map<String, Object> getBasicSettings() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(EmailConnector.SMTP_HOST, "localhost");
        parameters.put(EmailConnector.SMTP_PORT, 25);
        parameters.put(EmailConnector.TO, ADDRESSJOHN);
        parameters.put(EmailConnector.SUBJECT, SUBJECT);
        parameters.put(EmailConnector.SSL_SUPPORT, false);
        parameters.put(EmailConnector.HTML, false);
        return parameters;
    }

    private Map<String, Object> getXOAuth2Settings() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(EmailConnector.SMTP_HOST, "smtp.office365.com");
        parameters.put(EmailConnector.SMTP_PORT, 587);
        parameters.put(EmailConnector.SSL_SUPPORT, false);
        parameters.put(EmailConnector.STARTTLS_SUPPORT, true);
        parameters.put(EmailConnector.AUTH_TYPE, EmailConnector.XOAUTH2_AUTH_TYPE);
        parameters.put(EmailConnector.USER_NAME, "user@company.com");
        parameters.put(EmailConnector.OAUTH2_ACCESS_TOKEN, "mock_access_token");
        parameters.put(EmailConnector.TO, ADDRESSJOHN);
        parameters.put(EmailConnector.SUBJECT, SUBJECT);
        parameters.put(EmailConnector.HTML, false);
        return parameters;
    }

    private void setConnectorParameters(Map<String, Object> parameters, String userEmail, String accessToken, String recipientEmail) {
        parameters.put(EmailConnector.SMTP_PORT, 587);
        parameters.put(EmailConnector.SSL_SUPPORT, false);
        parameters.put(EmailConnector.STARTTLS_SUPPORT, true);
        parameters.put(EmailConnector.AUTH_TYPE, EmailConnector.XOAUTH2_AUTH_TYPE);
        parameters.put(EmailConnector.USER_NAME, userEmail);
        parameters.put(EmailConnector.OAUTH2_ACCESS_TOKEN, accessToken);
        parameters.put(EmailConnector.FROM, userEmail);
        parameters.put(EmailConnector.TO, recipientEmail);
        parameters.put(EmailConnector.SUBJECT, "XOAUTH2 Test from Bonita Connector");
        parameters.put(EmailConnector.MESSAGE, "This email was sent using XOAUTH2 authentication.");
        parameters.put(EmailConnector.HTML, false);
    }
}
