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
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.engine.api.APIAccessor;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.document.Document;
import org.bonitasoft.engine.bpm.document.DocumentNotFoundException;
import org.bonitasoft.engine.bpm.document.impl.DocumentImpl;
import org.bonitasoft.engine.connector.ConnectorException;
import org.bonitasoft.engine.connector.ConnectorValidationException;
import org.bonitasoft.engine.connector.EngineExecutionContext;
import org.bonitasoft.engine.exception.BonitaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EmailConnectorIT {

    @RegisterExtension
    static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP);
    
    @RegisterExtension
    static GreenMailExtension sslGreenMail = new GreenMailExtension(ServerSetupTest.SMTPS);

    @Mock
    private EngineExecutionContext engineExecutionContext;
    @Mock
    private APIAccessor apiAccessor;
    @Mock
    private ProcessAPI processAPI;

    private static final String ADDRESSJOHN = "john.doe@bonita.org";
    private static final String ADDRESSPATTY = "patty.johnson@gmal.com";
    private static final String ADDRESSMARK = "mark.hunt@wahoo.nz";
    private static final String SUBJECT = "Testing EmailConnector";
    private static final String PLAINMESSAGE = "Plain Message";
    private static final String HTMLMESSAGE = "<b><i>HTML<i/> Message</b>";
    private static final String TEXT_PLAIN = "text/plain";
    private static final String TEXT_HTML = "text/html";
    private static final String CYRILLIC_SUBJECT = "\u0416 \u0414 \u0431";
    private static final String CYRILLIC_MESSAGE = "\u0416 \u0414 \u0431";

    @BeforeEach
    public void setUp() {
        when(apiAccessor.getProcessAPI()).thenReturn(processAPI);
    }

    @Test
    void should_send_a_simple_email() throws Exception {
        executeConnector(getBasicSettings());
        final MimeMessage[] messages = greenMail.getReceivedMessages();
        assertEquals(1, messages.length);
        final MimeMessage message = messages[0];
        assertThat(message.getFrom()).hasSize(1);
        assertThat(message.getRecipients(RecipientType.TO)[0]).hasToString(ADDRESSJOHN);
        assertThat(message.getSubject()).isEqualTo(SUBJECT);
    }

    @Test
    void testSendEmailWithFromAddress() throws Exception {
        final Map<String, Object> parameters = getBasicSettings();
        parameters.put("from", ADDRESSMARK);
        executeConnector(parameters);
        final MimeMessage[] messages = greenMail.getReceivedMessages();
        assertEquals(1, messages.length);
        final MimeMessage message = messages[0];
        assertThat(message.getFrom()).hasSize(1);
        assertThat(message.getFrom()[0]).hasToString(ADDRESSMARK);
        assertThat(message.getRecipients(RecipientType.TO)[0]).hasToString(ADDRESSJOHN);
        assertThat(message.getSubject()).isEqualTo(SUBJECT);
    }

    @Test
    void testSendEmailWithAutentication() throws Exception {
        greenMail.setUser("john.doe", "bpm");
        final Map<String, Object> parameters = getBasicSettings();
        parameters.put("userName", "john.doe");
        parameters.put("password", "bpm");
        executeConnector(parameters);
        final MimeMessage[] messages = greenMail.getReceivedMessages();
        assertEquals(1, messages.length);
        final MimeMessage message = messages[0];
        assertThat(message.getFrom()).hasSize(1);
        assertThat(message.getRecipients(RecipientType.TO)[0]).hasToString(ADDRESSJOHN);
        assertThat(message.getSubject()).isEqualTo(SUBJECT);
    }

    @Test
    void should_connector_not_fail_if_an_header_line_contains_only_one_element() throws Exception {
        List<List<Object>> headers = new ArrayList<>();
        List<Object> line = new ArrayList<>();
        line.add("");
        headers.add(line);
        Map<String, Object> parameters = getBasicSettings();
        parameters.put("headers", headers);

        executeConnector(parameters);
    }

    @Test
    void sendEmailWithToRecipientsAddresses() throws Exception {
        final Map<String, Object> parameters = getBasicSettings();
        parameters.put("to", ADDRESSJOHN + ", " + ADDRESSPATTY);
        parameters.put("from", ADDRESSMARK);
        executeConnector(parameters);
        final MimeMessage[] messages = greenMail.getReceivedMessages();
        assertEquals(2, messages.length);
        final MimeMessage message = messages[0];
        assertThat(message.getFrom()).hasSize(1);
        assertThat(message.getFrom()[0]).hasToString(ADDRESSMARK);
        assertThat(message.getRecipients(RecipientType.TO)[0]).hasToString(ADDRESSJOHN);
        assertThat(message.getRecipients(RecipientType.TO)[1]).hasToString(ADDRESSPATTY);
        assertThat(message.getSubject()).isEqualTo(SUBJECT);

        final MimeMessage message2 = messages[1];
        assertThat(message2.getFrom()).hasSize(1);
        assertThat(message2.getFrom()[0]).hasToString(ADDRESSMARK);
        assertThat(message2.getRecipients(RecipientType.TO)[1]).hasToString(ADDRESSPATTY);
        assertThat(message2.getSubject()).isEqualTo(SUBJECT);
    }

    @Test
    void sendEmailWithCcRecipientsAddresses() throws Exception {
        final Map<String, Object> parameters = getBasicSettings();
        parameters.put("cc", ADDRESSPATTY);
        parameters.put("from", ADDRESSMARK);
        executeConnector(parameters);

        MimeMessage[] messages = greenMail.getReceivedMessages();
        assertEquals(2, messages.length);
        MimeMessage message = messages[0];
        assertThat(message.getFrom()).hasSize(1);
        assertThat(message.getFrom()[0]).hasToString(ADDRESSMARK);
        assertThat(message.getRecipients(RecipientType.TO)[0]).hasToString(ADDRESSJOHN);
        assertThat(message.getRecipients(RecipientType.CC)[0]).hasToString(ADDRESSPATTY);
        assertThat(message.getSubject()).isEqualTo(SUBJECT);

        greenMail.reset();
        parameters.put("cc", ADDRESSPATTY + ", " + ADDRESSMARK);
        executeConnector(parameters);

        messages = greenMail.getReceivedMessages();
        assertEquals(3, messages.length);
        message = messages[0];
        assertThat(message.getFrom()).hasSize(1);
        assertThat(message.getFrom()[0]).hasToString(ADDRESSMARK);
        assertThat(message.getRecipients(RecipientType.TO)[0]).hasToString(ADDRESSJOHN);
        assertThat(message.getRecipients(RecipientType.CC)[0]).hasToString(ADDRESSPATTY);
        assertThat(message.getRecipients(RecipientType.CC)[1]).hasToString(ADDRESSMARK);
        assertThat(message.getSubject()).isEqualTo(SUBJECT);
    }

    @Test
    void sendEmailWithReturnPathAddress() throws Exception {
        final Map<String, Object> parameters = getBasicSettings();
        parameters.put("returnPath", ADDRESSPATTY);
        parameters.put("from", ADDRESSMARK);
        executeConnector(parameters);

        MimeMessage[] messages = greenMail.getReceivedMessages();
        assertEquals(1, messages.length);
        MimeMessage message = messages[0];
        assertThat(message.getFrom()).hasSize(1);
        assertThat(message.getFrom()[0]).hasToString(ADDRESSMARK);
        assertThat(message.getHeader("Return-path")[0]).hasToString("<" + ADDRESSPATTY + ">");
        assertThat(message.getRecipients(RecipientType.TO)[0]).hasToString(ADDRESSJOHN);
        assertThat(message.getSubject()).isEqualTo(SUBJECT);
    }

    @Test
    void sendEmailWithPlainMessage() throws Exception {
        final Map<String, Object> parameters = getBasicSettings();
        parameters.put("message", PLAINMESSAGE);
        parameters.put("from", ADDRESSMARK);
        executeConnector(parameters);

        MimeMessage[] messages = greenMail.getReceivedMessages();
        assertEquals(1, messages.length);
        MimeMessage message = messages[0];
        assertThat(message.getFrom()).hasSize(1);
        assertThat(message.getFrom()[0]).hasToString(ADDRESSMARK);
        assertThat(message.getRecipients(RecipientType.TO)[0]).hasToString(ADDRESSJOHN);
        assertThat(message.getSubject()).isEqualTo(SUBJECT);
        assertThat(message.getContentType()).contains(TEXT_PLAIN);
        assertThat(GreenMailUtil.getBody(message)).isEqualTo(PLAINMESSAGE);
    }

    @Test
    void sendEmailWithHtmlMessage() throws Exception {
        final Map<String, Object> parameters = getBasicSettings();
        parameters.put("html", true);
        parameters.put("message", HTMLMESSAGE);
        parameters.put("from", ADDRESSMARK);
        executeConnector(parameters);

        MimeMessage[] messages = greenMail.getReceivedMessages();
        assertEquals(1, messages.length);
        MimeMessage message = messages[0];
        assertThat(message.getFrom()).hasSize(1);
        assertThat(message.getFrom()[0]).hasToString(ADDRESSMARK);
        assertThat(message.getRecipients(RecipientType.TO)[0]).hasToString(ADDRESSJOHN);
        assertThat(message.getSubject()).isEqualTo(SUBJECT);
        assertThat(message.getContentType()).contains(TEXT_HTML);
        assertThat(GreenMailUtil.getBody(message)).isEqualTo(HTMLMESSAGE);
    }

    @Test
    void sendCyrillicEmail() throws Exception {
        final Map<String, Object> parameters = getBasicSettings();
        parameters.put("subject", CYRILLIC_SUBJECT);
        parameters.put("message", CYRILLIC_MESSAGE);
        executeConnector(parameters);

        MimeMessage[] messages = greenMail.getReceivedMessages();
        assertEquals(1, messages.length);
        MimeMessage message = messages[0];
        assertThat(message.getFrom()).hasSize(1);
        assertThat(message.getRecipients(RecipientType.TO)[0]).hasToString(ADDRESSJOHN);
        assertThat(message.getSubject()).isEqualTo(CYRILLIC_SUBJECT);
        assertThat(message.getContentType()).contains(TEXT_PLAIN);
        assertThat(message.getContent()).isEqualTo(CYRILLIC_MESSAGE);
    }

    @Test
    void sendBadEncodingCyrillicEmail() throws Exception {
        final Map<String, Object> parameters = getBasicSettings();
        parameters.put("charset", "iso-8859-1");
        parameters.put("message", CYRILLIC_MESSAGE);
        executeConnector(parameters);

        MimeMessage[] messages = greenMail.getReceivedMessages();
        assertEquals(1, messages.length);
        MimeMessage message = messages[0];
        assertThat(message.getFrom()).hasSize(1);
        assertThat(message.getRecipients(RecipientType.TO)[0]).hasToString(ADDRESSJOHN);
        assertThat(message.getContentType()).contains(TEXT_PLAIN);
        assertThat(GreenMailUtil.getBody(message)).isEqualTo("? ? ?");
    }

    @Test
    void sendEmailWithExtraHeaders() throws Exception {
        final List<List<String>> headers = new ArrayList<>();
        List<String> row1 = new ArrayList<>();
        row1.add("X-Mailer");
        row1.add("Bonita Mailer");
        headers.add(row1);

        List<String> row2 = new ArrayList<>();
        row2.add("Message-ID");
        row2.add("IWantToHackTheServer");
        headers.add(row2);

        List<String> row3 = new ArrayList<>();
        row3.add("X-Priority");
        row3.add("2 (High)");
        headers.add(row3);

        List<String> row4 = new ArrayList<>();
        row4.add("Content-Type");
        row4.add("video/mpeg");
        headers.add(row4);

        List<String> row5 = new ArrayList<>();
        row5.add("WhatIWant");
        row5.add("anyValue");
        headers.add(row5);

        List<String> row6 = new ArrayList<>();
        row6.add("From");
        row6.add("alice@bob.charly");
        headers.add(row6);

        List<String> row7 = new ArrayList<>();
        row7.add(null);
        row7.add(null);
        headers.add(row7);

        final Map<String, Object> parameters = getBasicSettings();

        parameters.put("headers", headers);

        executeConnector(parameters);

        MimeMessage[] messages = greenMail.getReceivedMessages();
        assertEquals(1, messages.length);
        MimeMessage message = messages[0];
        assertThat(message.getFrom()).hasSize(1);
        assertThat(message.getFrom()[0]).hasToString("alice@bob.charly");
        assertThat(message.getRecipients(RecipientType.TO)[0]).hasToString(ADDRESSJOHN);
        assertThat(message.getSubject()).isEqualTo(SUBJECT);
        assertThat(GreenMailUtil.getHeaders(message))
                .contains("X-Mailer: Bonita Mailer")
                .contains("WhatIWant: anyValue")
                .contains("X-Priority: 2 (High)")
                .contains("From: alice@bob.charly");
        assertThat(message.getHeader("Content-Type")[0]).isEqualTo("text/plain; charset=UTF-8");
        assertThat(message.getHeader("Message-ID")[0]).isNotEqualTo("IWantToHackTheServer");
    }

    @Test
    void sendMailWithEmptyDocument() throws Exception {
        DocumentImpl document = new DocumentImpl();
        document.setAuthor(1);
        document.setCreationDate(new Date());
        document.setId(1);
        document.setProcessInstanceId(1);
        document.setName("Document1");
        when(engineExecutionContext.getProcessInstanceId()).thenReturn(1L);
        when(processAPI.getLastDocument(1L, "Document1")).thenReturn(document);
        Map<String, Object> parameters = getBasicSettings();
        parameters.put(EmailConnector.MESSAGE, "Hello Mr message\n This is an email content");
        List<String> attachments = Collections.singletonList("Document1");
        parameters.put(EmailConnector.ATTACHMENTS, attachments);

        executeConnector(parameters);

        MimeMessage[] messages = greenMail.getReceivedMessages();
        assertEquals(1, messages.length);
        MimeMessage message = messages[0];
        assertThat((String) ((MimeMultipart) message.getContent()).getBodyPart(0).getContent())
                .doesNotContain("Document1");
    }

    @Test
    void sendUrlDocument() throws Exception {
        DocumentImpl document = new DocumentImpl();
        document.setAuthor(1);
        document.setCreationDate(new Date());
        document.setHasContent(false);
        document.setId(1);
        document.setProcessInstanceId(1);
        document.setUrl("http://www.bonitasoft.com");
        document.setName("Document1");
        when(engineExecutionContext.getProcessInstanceId()).thenReturn(1L);
        when(processAPI.getLastDocument(1L, "Document1")).thenReturn(document);
        Map<String, Object> parameters = getBasicSettings();
        parameters.put(EmailConnector.MESSAGE, "Hello Mr message\n This is an email content");
        List<String> attachments = Collections.singletonList("Document1");
        parameters.put(EmailConnector.ATTACHMENTS, attachments);

        executeConnector(parameters);

        MimeMessage[] messages = greenMail.getReceivedMessages();
        assertEquals(1, messages.length);
        assertThat(messages[0].getContentType()).startsWith("multipart/mixed;");

        final Multipart part = (Multipart) messages[0].getContent();
        assertThat((String) part.getBodyPart(0).getContent())
                .contains("http://www.bonitasoft.com");
    }

    @Test
    void sendWithMultipleDocumentList() throws Exception {
        DocumentImpl document1 = createDocument(1L, "toto1");
        DocumentImpl document2 = createDocument(2L, "toto2");
        DocumentImpl document3 = createDocument(3L, "toto3");
        DocumentImpl document4 = createDocument(4L, "toto4");
        List<Document> documents1 = Arrays.<Document> asList(document1, document2);
        List<Document> documents2 = Arrays.<Document> asList(document3, document4);
        List<?> lists = Arrays.asList(documents1, documents2);
        Map<String, Object> parameters = getBasicSettings();
        parameters.put(EmailConnector.ATTACHMENTS, lists);

        executeConnector(parameters);

        MimeMessage[] messages = greenMail.getReceivedMessages();
        assertEquals(1, messages.length);
        assertThat(messages[0].getContentType()).startsWith("multipart/mixed;");

        List<byte[]> contents = getAttachmentsContent((MimeMultipart) (Multipart) messages[0].getContent());
        assertThat(new String(contents.get(1))).isEqualTo("toto1");
        assertThat(new String(contents.get(2))).isEqualTo("toto2");
        assertThat(new String(contents.get(3))).isEqualTo("toto3");
        assertThat(new String(contents.get(4))).isEqualTo("toto4");
    }

    @Test
    void sendWithDocumentList() throws Exception {
        DocumentImpl document1 = createDocument(1L, "toto1");
        DocumentImpl document2 = createDocument(2L, "toto2");
        List<Document> documents = Arrays.<Document> asList(document1, document2);
        Map<String, Object> parameters = getBasicSettings();
        parameters.put(EmailConnector.ATTACHMENTS, documents);

        executeConnector(parameters);

        MimeMessage[] messages = greenMail.getReceivedMessages();
        assertEquals(1, messages.length);
        assertThat(messages[0].getContentType()).startsWith("multipart/mixed;");

        List<byte[]> contents = getAttachmentsContent((MimeMultipart) (Multipart) messages[0].getContent());
        assertThat(new String(contents.get(1))).isEqualTo("toto1");
        assertThat(new String(contents.get(2))).isEqualTo("toto2");
    }

    @Test
    void sendFileDocument() throws BonitaException, MessagingException, IOException {
        DocumentImpl document = new DocumentImpl();
        document.setAuthor(1);
        document.setContentMimeType("application/octet-stream");
        document.setContentStorageId("storageId");
        document.setCreationDate(new Date());
        document.setFileName("filename.txt");
        document.setHasContent(true);
        document.setId(1);
        document.setProcessInstanceId(1);
        document.setName("Document1");
        when(engineExecutionContext.getProcessInstanceId()).thenReturn(1L);
        when(processAPI.getLastDocument(1L, "Document1")).thenReturn(document);
        when(processAPI.getDocumentContent("storageId")).thenReturn("toto".getBytes());
        Map<String, Object> parameters = getBasicSettings();
        List<String> attachments = Collections.singletonList("Document1");
        parameters.put(EmailConnector.ATTACHMENTS, attachments);

        executeConnector(parameters);

        MimeMessage[] messages = greenMail.getReceivedMessages();
        assertEquals(1, messages.length);
        assertThat(messages[0].getContentType()).startsWith("multipart/mixed;");
        assertThat(((MimeMultipart) messages[0].getContent()).getBodyPart(1).getFileName())
                .isEqualTo("filename.txt");
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

    private Map<String, Object> getBasicSettings() {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("smtpHost", greenMail.getSmtp().getBindTo());
        parameters.put("smtpPort", greenMail.getSmtp().getPort());
        parameters.put("to", ADDRESSJOHN);
        parameters.put("subject", SUBJECT);
        parameters.put("sslSupport", false);
        parameters.put("html", false);
        return parameters;
    }

    private List<byte[]> getAttachmentsContent(MimeMultipart multipart) throws MessagingException, IOException {
        List<byte[]> attachments = new ArrayList<>();
        for (int i = 0; i < multipart.getCount(); i++) {
            BodyPart bodyPart = multipart.getBodyPart(i);
            attachments.add(IOUtils.toByteArray(bodyPart.getInputStream()));
        }
        return attachments;
    }

    private DocumentImpl createDocument(long id, String content) throws DocumentNotFoundException {
        DocumentImpl document = new DocumentImpl();
        document.setAuthor(1);
        document.setContentMimeType("application/octet-stream");
        document.setContentStorageId("storageId" + id);
        document.setCreationDate(new Date());
        document.setFileName("filename.txt");
        document.setHasContent(true);
        document.setId(id);
        document.setProcessInstanceId(1);
        document.setName("Document1");
        when(processAPI.getDocumentContent("storageId" + id)).thenReturn(content.getBytes());
        return document;
    }

}
