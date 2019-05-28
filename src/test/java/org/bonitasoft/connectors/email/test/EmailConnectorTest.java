/**
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.connectors.email.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeNotNull;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.connectors.email.EmailConnector;
import org.bonitasoft.engine.api.APIAccessor;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.document.Document;
import org.bonitasoft.engine.bpm.document.DocumentNotFoundException;
import org.bonitasoft.engine.bpm.document.impl.DocumentImpl;
import org.bonitasoft.engine.connector.EngineExecutionContext;
import org.bonitasoft.engine.exception.BonitaException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.subethamail.wiser.Wiser;
import org.subethamail.wiser.WiserMessage;

// import org.bonitasoft.engine.test.annotation.Cover;
// import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;

/**
 * @author Matthieu Chaffotte
 */
@RunWith(MockitoJUnitRunner.class)
public class EmailConnectorTest {

    private static final String SMTP_HOST = "localhost";

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

    private static int smtpPort = 0;

    private Wiser server;
    @Mock
    private EngineExecutionContext engineExecutionContext;
    @Mock
    private APIAccessor apiAccessor;
    @Mock
    private ProcessAPI processAPI;

    @Before
    public void setUp() {
        when(apiAccessor.getProcessAPI()).thenReturn(processAPI);
        startServer();
    }

    @After
    public void tearDown() throws InterruptedException {
        if (server != null) {
            stopServer();
        }
    }

    private void startServer() {
        if (smtpPort == 0) {
            smtpPort = findFreePort(31025);
        }
        server = new Wiser();
        server.setPort(smtpPort);
        server.start();
    }

    private static int findFreePort(int port) {
        boolean free = false;
        while (!free && port <= 65535) {
            if (isFreePort(port)) {
                free = true;
            } else {
                port++;
            }
        }
        return port;
    }

    private static boolean isFreePort(final int port) {
        try {
            final ServerSocket socket = new ServerSocket(port);
            socket.close();
            return true;
        } catch (final IOException e) {
            return false;
        }
    }

    private void stopServer() throws InterruptedException {
        server.stop();
        Thread.sleep(150);
        server = null;
    }

    private Map<String, Object> executeConnector(final Map<String, Object> parameters) throws BonitaException {
        final EmailConnector email = new EmailConnector();
        email.setExecutionContext(engineExecutionContext);
        email.setAPIAccessor(apiAccessor);
        email.setInputParameters(parameters);
        email.validateInputParameters();
        return email.execute();
    }

    private Map<String, Object> getBasicSettings() {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("smtpHost", SMTP_HOST);
        parameters.put("smtpPort", smtpPort);
        parameters.put("to", ADDRESSJOHN);
        parameters.put("subject", SUBJECT);
        parameters.put("sslSupport", false);
        parameters.put("html", false);
        return parameters;
    }

    //    @Cover(classes = { EmailConnector.class }, concept = BPMNConcept.CONNECTOR, keywords = { "email" },
    //            story = "Test the sending of a simple email through the connector", jira = "")
    @Test
    public void sendASimpleEmail() throws BonitaException, MessagingException, InterruptedException {
        executeConnector(getBasicSettings());
        final List<WiserMessage> messages = server.getMessages();
        assertEquals(1, messages.size());
        final WiserMessage message = messages.get(0);
        assertNotNull(message.getEnvelopeSender());
        assertEquals(ADDRESSJOHN, message.getEnvelopeReceiver());
        final MimeMessage mime = message.getMimeMessage();
        assertEquals(SUBJECT, mime.getSubject());
        assertEquals(0, mime.getSize());
    }

    //    @Cover(classes = { EmailConnector.class }, concept = BPMNConcept.CONNECTOR, keywords = { "email" },
    //            story = "Test the sending of a email with field from filled through the connector", jira = "")
    @Test
    public void testSendEmailWithFromAddress() throws Exception {
        final Map<String, Object> parameters = getBasicSettings();
        parameters.put("from", ADDRESSMARK);
        executeConnector(parameters);
        final List<WiserMessage> messages = server.getMessages();
        assertEquals(1, messages.size());
        final WiserMessage message = messages.get(0);
        assertEquals(ADDRESSMARK, message.getEnvelopeSender());
        assertEquals(ADDRESSJOHN, message.getEnvelopeReceiver());
        final MimeMessage mime = message.getMimeMessage();
        assertEquals(SUBJECT, mime.getSubject());
        assertEquals(0, mime.getSize());
    }

    //    @Cover(classes = { EmailConnector.class }, concept = BPMNConcept.CONNECTOR, keywords = { "email" },
    //            story = "Test the sending of a email with authentification through the connector", jira = "")
    @Test
    public void testSendEmailWithAutentication() throws Exception {
        final Map<String, Object> parameters = getBasicSettings();
        parameters.put("userName", "john");
        parameters.put("password", "doe");
        executeConnector(parameters);
        final List<WiserMessage> messages = server.getMessages();
        assertEquals(1, messages.size());
        final WiserMessage message = messages.get(0);
        assertNotNull(message.getEnvelopeSender());
        assertEquals(ADDRESSJOHN, message.getEnvelopeReceiver());
        final MimeMessage mime = message.getMimeMessage();
        assertEquals(SUBJECT, mime.getSubject());
        assertEquals(0, mime.getSize());
    }

    // BI-284 - [6.0.2] Email connector fails if one header row is empty
    @Test
    public void connector_dont_fail_if_an_header_line_contains_only_one_element() throws Exception {
        List<List<Object>> headers = new ArrayList<List<Object>>();
        List<Object> line = new ArrayList<Object>();
        line.add("");
        headers.add(line);
        Map<String, Object> parameters = getBasicSettings();
        parameters.put("headers", headers);

        executeConnector(parameters);
    }

    @Test
    public void sendEmailWithToRecipientsAddresses() throws Exception {
        final Map<String, Object> parameters = getBasicSettings();
        parameters.put("to", ADDRESSJOHN + ", " + ADDRESSPATTY);
        parameters.put("from", ADDRESSMARK);
        executeConnector(parameters);
        final List<WiserMessage> messages = server.getMessages();
        assertEquals(2, messages.size());
        WiserMessage message = messages.get(0);
        assertEquals(ADDRESSMARK, message.getEnvelopeSender());
        assertEquals(ADDRESSJOHN, message.getEnvelopeReceiver());
        MimeMessage mime = message.getMimeMessage();
        assertEquals(SUBJECT, mime.getSubject());
        assertEquals(0, mime.getSize());

        message = messages.get(1);
        assertEquals(ADDRESSMARK, message.getEnvelopeSender());
        assertEquals(ADDRESSPATTY, message.getEnvelopeReceiver());
        mime = message.getMimeMessage();
        assertEquals(SUBJECT, mime.getSubject());
        assertEquals(0, mime.getSize());
    }

    @Test
    public void sendEmailWithCcRecipientsAddresses() throws Exception {
        final Map<String, Object> parameters = getBasicSettings();
        parameters.put("cc", ADDRESSPATTY);
        parameters.put("from", ADDRESSMARK);
        executeConnector(parameters);

        List<WiserMessage> messages = server.getMessages();
        assertEquals(2, messages.size());
        WiserMessage message = messages.get(0);
        assertEquals(ADDRESSMARK, message.getEnvelopeSender());
        assertEquals(ADDRESSJOHN, message.getEnvelopeReceiver());
        MimeMessage mime = message.getMimeMessage();
        assertEquals(SUBJECT, mime.getSubject());
        assertEquals(0, mime.getSize());

        message = messages.get(1);
        assertEquals(ADDRESSMARK, message.getEnvelopeSender());
        assertEquals(ADDRESSPATTY, message.getEnvelopeReceiver());
        mime = message.getMimeMessage();
        assertEquals(SUBJECT, mime.getSubject());
        assertEquals(0, mime.getSize());

        parameters.put("cc", ADDRESSPATTY + ", " + ADDRESSMARK);
        executeConnector(parameters);
        messages = server.getMessages();
        assertEquals(5, messages.size());
        message = messages.get(4);
        assertEquals(ADDRESSMARK, message.getEnvelopeSender());
        assertEquals(ADDRESSMARK, message.getEnvelopeReceiver());
        mime = message.getMimeMessage();
        assertEquals(SUBJECT, mime.getSubject());
        assertEquals(0, mime.getSize());
    }
    
    @Test
    public void sendEmailWithReturnPathAddress() throws Exception {
        final Map<String, Object> parameters = getBasicSettings();
        parameters.put("returnPath", ADDRESSPATTY);
        parameters.put("from", ADDRESSMARK);
        executeConnector(parameters);

        List<WiserMessage> messages = server.getMessages();
        assertEquals(1, messages.size());
        WiserMessage message = messages.get(0);
        assertEquals(ADDRESSPATTY, message.getEnvelopeSender());
        assertEquals(new InternetAddress(ADDRESSMARK), message.getMimeMessage().getFrom()[0]);
        assertEquals(ADDRESSJOHN, message.getEnvelopeReceiver());
    }

    @Test
    public void sendEmailWithPlainMessage() throws Exception {
        final Map<String, Object> parameters = getBasicSettings();
        parameters.put("message", PLAINMESSAGE);
        parameters.put("from", ADDRESSMARK);
        executeConnector(parameters);

        final List<WiserMessage> messages = server.getMessages();
        assertEquals(1, messages.size());
        final WiserMessage message = messages.get(0);
        assertEquals(ADDRESSMARK, message.getEnvelopeSender());
        assertEquals(ADDRESSJOHN, message.getEnvelopeReceiver());
        final MimeMessage mime = message.getMimeMessage();
        assertEquals(SUBJECT, mime.getSubject());
        assertTrue(mime.getContentType().contains(TEXT_PLAIN));
        assertEquals(PLAINMESSAGE, mime.getContent());
    }

    @Test
    public void sendEmailWithHtmlMessage() throws Exception {
        final Map<String, Object> parameters = getBasicSettings();
        parameters.put("html", true);
        parameters.put("message", HTMLMESSAGE);
        parameters.put("from", ADDRESSMARK);
        executeConnector(parameters);

        final List<WiserMessage> messages = server.getMessages();
        assertEquals(1, messages.size());
        final WiserMessage message = messages.get(0);
        assertEquals(ADDRESSMARK, message.getEnvelopeSender());
        assertEquals(ADDRESSJOHN, message.getEnvelopeReceiver());
        final MimeMessage mime = message.getMimeMessage();
        assertEquals(SUBJECT, mime.getSubject());
        assertTrue(mime.getContentType().contains(TEXT_HTML));
        assertEquals(HTMLMESSAGE, mime.getContent());
    }

    @Test
    public void sendEmailWithExtraHeaders() throws Exception {
        final List<List<String>> headers = new ArrayList<List<String>>();
        List<String> row1 = new ArrayList<String>();
        row1.add("X-Mailer");
        row1.add("Bonita Mailer");
        headers.add(row1);

        List<String> row2 = new ArrayList<String>();
        row2.add("Message-ID");
        row2.add("IWantToHackTheServer");
        headers.add(row2);

        List<String> row3 = new ArrayList<String>();
        row3.add("X-Priority");
        row3.add("2 (High)");
        headers.add(row3);

        List<String> row4 = new ArrayList<String>();
        row4.add("Content-Type");
        row4.add("video/mpeg");
        headers.add(row4);

        List<String> row5 = new ArrayList<String>();
        row5.add("WhatIWant");
        row5.add("anyValue");
        headers.add(row5);

        List<String> row6 = new ArrayList<String>();
        row6.add("From");
        row6.add("alice@bob.charly");
        headers.add(row6);

        List<String> row7 = new ArrayList<String>();
        row7.add(null);
        row7.add(null);
        headers.add(row7);

        final Map<String, Object> parameters = getBasicSettings();

        parameters.put("headers", headers);

        executeConnector(parameters);

        final List<WiserMessage> messages = server.getMessages();
        assertEquals(1, messages.size());
        final WiserMessage message = messages.get(0);
        assertEquals("alice@bob.charly", message.getEnvelopeSender());
        assertEquals(ADDRESSJOHN, message.getEnvelopeReceiver());
        final MimeMessage mime = message.getMimeMessage();
        assertEquals(SUBJECT, mime.getSubject());
        assertEquals(0, mime.getSize());
        assertEquals("Bonita Mailer", mime.getHeader("X-Mailer", ""));
        assertEquals("2 (High)", mime.getHeader("X-Priority", ""));
        assertEquals("anyValue", mime.getHeader("WhatIWant", ""));
        assertNotSame("alice@bob.charly", mime.getHeader("From"));
        assertFalse(mime.getContentType().contains("video/mpeg"));
        assertNotSame("IWantToHackTheServer", mime.getHeader("Message-ID"));
    }

    @Test
    public void sendCyrillicEmail() throws Exception {
        final Map<String, Object> parameters = getBasicSettings();
        parameters.put("subject", CYRILLIC_SUBJECT);
        parameters.put("message", CYRILLIC_MESSAGE);
        executeConnector(parameters);

        final List<WiserMessage> messages = server.getMessages();
        assertEquals(1, messages.size());
        final WiserMessage message = messages.get(0);
        assertNotNull(message.getEnvelopeSender());
        assertEquals(ADDRESSJOHN, message.getEnvelopeReceiver());
        final MimeMessage mime = message.getMimeMessage();
        assertEquals(CYRILLIC_SUBJECT, mime.getSubject());
        assertTrue(mime.getContentType().contains(TEXT_PLAIN));
        assertEquals(CYRILLIC_MESSAGE, mime.getContent());
    }

    @Test
    public void sendBadEncodingCyrillicEmail() throws Exception {
        final Map<String, Object> parameters = getBasicSettings();
        parameters.put("charset", "iso-8859-1");
        parameters.put("message", CYRILLIC_MESSAGE);
        executeConnector(parameters);

        final List<WiserMessage> messages = server.getMessages();
        assertEquals(1, messages.size());
        final WiserMessage message = messages.get(0);
        assertNotNull(message.getEnvelopeSender());
        assertEquals(ADDRESSJOHN, message.getEnvelopeReceiver());
        final MimeMessage mime = message.getMimeMessage();
        assertEquals(SUBJECT, mime.getSubject());
        assertTrue(mime.getContentType().contains(TEXT_PLAIN));
        assertEquals("? ? ?", mime.getContent());
    }

    @Test
    public void sendFileDocument() throws BonitaException, MessagingException, IOException {
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

        List<WiserMessage> messages = server.getMessages();
        assumeNotNull(messages);
        assertThat(((MimeMultipart) messages.get(0).getMimeMessage().getContent()).getBodyPart(1).getFileName()).isEqualTo("filename.txt");
        // BS-11239 : change multipart mime type in order to have attachments openable on iPhone
        assertThat(messages.get(0).getMimeMessage().getContentType()).startsWith("multipart/mixed;");
    }
    
    @Test
    public void sendFileDocumentWithSpecialEncoding() throws BonitaException, MessagingException, IOException {
        DocumentImpl document = new DocumentImpl();
        document.setAuthor(1);
        document.setContentMimeType("application/octet-stream");
        document.setContentStorageId("storageId");
        document.setCreationDate(new Date());
        document.setFileName("最日本最.TXT");
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

        List<WiserMessage> messages = server.getMessages();
        assumeNotNull(messages);
        assertThat(((MimeMultipart) messages.get(0).getMimeMessage().getContent()).getBodyPart(1).getFileName()).isEqualTo(MimeUtility.encodeText("最日本最.TXT"));
    }

    @Test
    public void sendWithDocumentList() throws BonitaException, MessagingException, IOException {
        DocumentImpl document1 = createDocument(1L, "toto1");
        DocumentImpl document2 = createDocument(2L, "toto2");
        List<Document> documents = Arrays.<Document> asList(document1, document2);
        Map<String, Object> parameters = getBasicSettings();
        parameters.put(EmailConnector.ATTACHMENTS, documents);

        executeConnector(parameters);

        List<WiserMessage> messages = server.getMessages();
        assumeNotNull(messages);
        List<byte[]> contents = getAttachmentsContent((MimeMultipart) messages.get(0).getMimeMessage().getContent());
        assertThat(new String(contents.get(1))).isEqualTo("toto1");
        assertThat(new String(contents.get(2))).isEqualTo("toto2");
    }

    @Test
    public void sendWithMultipleDocumentList() throws BonitaException, MessagingException, IOException {
        DocumentImpl document1 = createDocument(1L, "toto1");
        DocumentImpl document2 = createDocument(2L, "toto2");
        DocumentImpl document3 = createDocument(3L, "toto3");
        DocumentImpl document4 = createDocument(4L, "toto4");
        List<Document> documents1 = Arrays.<Document> asList(document1, document2);
        List<Document> documents2 = Arrays.<Document> asList(document3, document4);
        List lists = Arrays.asList(documents1, documents2);
        Map<String, Object> parameters = getBasicSettings();
        parameters.put(EmailConnector.ATTACHMENTS, lists);

        executeConnector(parameters);

        List<WiserMessage> messages = server.getMessages();
        assumeNotNull(messages);
        List<byte[]> contents = getAttachmentsContent((MimeMultipart) messages.get(0).getMimeMessage().getContent());
        assertThat(new String(contents.get(1))).isEqualTo("toto1");
        assertThat(new String(contents.get(2))).isEqualTo("toto2");
        assertThat(new String(contents.get(3))).isEqualTo("toto3");
        assertThat(new String(contents.get(4))).isEqualTo("toto4");
    }
    
    

    private List<byte[]> getAttachmentsContent(MimeMultipart multipart) throws MessagingException, IOException {
        List<byte[]> attachments = new ArrayList<byte[]>();
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

    @Test
    public void sendUrlDocument() throws BonitaException, MessagingException, IOException {
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

        List<WiserMessage> messages = server.getMessages();
        assumeNotNull(messages);
        assertThat((String) ((MimeMultipart) messages.get(0).getMimeMessage().getContent()).getBodyPart(0).getContent()).contains("http://www.bonitasoft.com");
    }

    @Test
    public void sendMailWithEmptyDocument() throws BonitaException, MessagingException, IOException {
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

        List<WiserMessage> messages = server.getMessages();
        assumeNotNull(messages);
        assertThat((String) ((MimeMultipart) messages.get(0).getMimeMessage().getContent()).getBodyPart(0).getContent()).doesNotContain("Document1");

    }

}
