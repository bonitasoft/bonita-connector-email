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

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.bonitasoft.engine.connector.ConnectorValidationException;
import org.bonitasoft.engine.exception.BonitaException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
class EmailConnectorValidationTest {

    private static final String SMTP_HOST = "localhost";

    private static final String ADDRESSJOHN = "john.doe@bonita.org";

    private static final String SUBJECT = "Testing EmailConnector";

    private static final String PLAINMESSAGE = "Plain Message";

    private void validateConnector(final Map<String, Object> parameters) throws ConnectorValidationException {
        final EmailConnector email = new EmailConnector();
        email.setInputParameters(parameters);
        email.validateInputParameters();
    }

    private Map<String, Object> getBasicSettings() {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("smtpHost", SMTP_HOST);
        int smtpPort = 0;
        parameters.put("smtpPort", smtpPort);
        parameters.put("to", ADDRESSJOHN);
        parameters.put("subject", SUBJECT);
        parameters.put("sslSupport", false);
        parameters.put("html", false);
        return parameters;
    }

    @Test
    void validatesSimpliestEmail() throws ConnectorValidationException {
        validateConnector(getBasicSettings());
    }

    @ParameterizedTest
    @MethodSource("provideValidInputs")
    void validInputParameters(String input, Object value) throws BonitaException {
        final Map<String, Object> parameters = getBasicSettings();
        parameters.put(input, value);
        validateConnector(parameters);
    }
    
    private static Stream<Arguments> provideValidInputs() {
        return Stream.of(
          Arguments.of("subject", SUBJECT),
          Arguments.of("subject", null),
          Arguments.of("message", PLAINMESSAGE),
          Arguments.of("message", ""),
          Arguments.of("message", null),
          Arguments.of("headers", null),
          Arguments.of("from", null),
          Arguments.of("from", "john@bpm.com")
        );
    }
    
    @Test
    void thowsExceptionDueToNoRecipientAddress() throws ConnectorValidationException {
        final Map<String, Object> parameters = getBasicSettings();
        parameters.remove("to");
        assertThrows(ConnectorValidationException.class, () -> validateConnector(parameters));
    }

    @Test
    void validEmailWithExtraHeaders() throws ConnectorValidationException {
        List<List<String>> headers = new ArrayList<List<String>>();
        List<String> line = new ArrayList<String>();
        line.add("X-Mailer");
        line.add("Bonita");
        headers.add(line);
        line = new ArrayList<String>();
        line.add("X-Sender");
        line.add("Test");
        line = new ArrayList<String>();
        line.add("WhatIwant");
        line.add("WhatIwant");
        final Map<String, Object> parameters = getBasicSettings();
        parameters.put("headers", headers);
        validateConnector(parameters);
    }


    @Test
    void validAuthentication() throws ConnectorValidationException {
        final Map<String, Object> parameters = getBasicSettings();
        parameters.put("usernName", null);
        parameters.put("password", null);
        validateConnector(parameters);

        parameters.put("usernName", "john");
        parameters.put("password", null);
        validateConnector(parameters);

        parameters.put("userName", null);
        parameters.put("password", "bonita");
        validateConnector(parameters);
    }

    
    @ParameterizedTest
    @MethodSource("provideInvalidInputs")
    void thowsExceptionDueToInvalidInputs(String input, Object value) throws ConnectorValidationException {
        final Map<String, Object> parameters = getBasicSettings();
        parameters.put(input,value);
        assertThrows(ConnectorValidationException.class, () -> validateConnector(parameters));
    }
    
    private static Stream<Arguments> provideInvalidInputs() {
        return Stream.of(
          Arguments.of("from", "@bonita.org"),
          Arguments.of("smtpHost", null),
          Arguments.of("smtpPort", null),
          Arguments.of("smtpPort", -1),
          Arguments.of("smtpPort", 65536)
        );
    }

}
