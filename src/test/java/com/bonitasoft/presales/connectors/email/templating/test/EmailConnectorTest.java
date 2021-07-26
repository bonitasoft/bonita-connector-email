package com.bonitasoft.presales.connectors.email.templating.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.bonitasoft.presales.connectors.email.templating.EmailConnector;
import java.io.IOException;
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
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import org.apache.commons.io.IOUtils;
import org.bonitasoft.engine.api.APIAccessor;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.document.Document;
import org.bonitasoft.engine.bpm.document.DocumentNotFoundException;
import org.bonitasoft.engine.bpm.document.impl.DocumentImpl;
import org.bonitasoft.engine.connector.EngineExecutionContext;
import org.bonitasoft.engine.exception.BonitaException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.subethamail.wiser.Wiser;
import org.subethamail.wiser.WiserMessage;

@ExtendWith(MockitoExtension.class)
public class EmailConnectorTest {

  private static final String LOCALHOST = "localhost";

  private static final String ADDRESSJOHN = "john.doe@bonita.org";

  private static final String ADDRESSPATTY = "patty.johnson@gmal.com";

  private static final String ADDRESSMARK = "mark.hunt@wahoo.nz";

  private static final String MAIL_SUBJECT = "Testing EmailConnector";

  private static final String PLAIN_MESSAGE = "Plain Message";

  private static final String HTML_MESSAGE = "<b><i>HTML<i/> Message</b>";

  private static final String TEXT_PLAIN = "text/plain";

  private static final String TEXT_HTML = "text/html";

  private static final String CYRILLIC_SUBJECT = "\u0416 \u0414 \u0431";

  private static final String CYRILLIC_MESSAGE = "\u0416 \u0414 \u0431";

  private static int smtpPort = 0;

  private Wiser server;

  @Mock(lenient = true)
  private EngineExecutionContext engineExecutionContext;

  @Mock(lenient = true)
  private APIAccessor apiAccessor;

  @Mock(lenient = true)
  private ProcessAPI processAPI;

  @BeforeEach
  public void setUp() {
    when(apiAccessor.getProcessAPI()).thenReturn(processAPI);
    startServer();
  }

  @AfterEach
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

  private Map<String, Object> executeConnector(final Map<String, Object> parameters)
      throws BonitaException {
    final EmailConnector email = new EmailConnector();
    email.setExecutionContext(engineExecutionContext);
    email.setAPIAccessor(apiAccessor);
    email.setInputParameters(parameters);
    email.validateInputParameters();
    return email.execute();
  }

  private Map<String, Object> getBasicSettings() {
    final Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("smtpHost", LOCALHOST);
    parameters.put("smtpPort", smtpPort);
    parameters.put("to", ADDRESSJOHN);
    parameters.put("subject", MAIL_SUBJECT);
    parameters.put("sslSupport", false);
    parameters.put("html", false);
    return parameters;
  }

  @Test
  public void sendASimpleEmail() throws BonitaException, MessagingException, InterruptedException {
    executeConnector(getBasicSettings());
    final List<WiserMessage> messages = server.getMessages();
    // FIXME // FIXME assertEquals(1, messages.size());
    final WiserMessage message = messages.get(0);
    // FIXME // FIXME assertNotNull(message.getEnvelopeSender());
    // FIXME assertEquals(ADDRESSJOHN, message.getEnvelopeReceiver());
    final MimeMessage mime = message.getMimeMessage();
    // FIXME assertEquals(MAIL_SUBJECT, mime.getSubject());
    // FIXME assertEquals(0, mime.getSize());
  }

  @Test
  public void testSendEmailWithFromAddress() throws Exception {
    final Map<String, Object> parameters = getBasicSettings();
    parameters.put("from", ADDRESSMARK);
    executeConnector(parameters);
    final List<WiserMessage> messages = server.getMessages();
    // FIXME assertEquals(1, messages.size());
    final WiserMessage message = messages.get(0);
    // FIXME assertEquals(ADDRESSMARK, message.getEnvelopeSender());
    // FIXME assertEquals(ADDRESSJOHN, message.getEnvelopeReceiver());
    final MimeMessage mime = message.getMimeMessage();
    // FIXME assertEquals(MAIL_SUBJECT, mime.getSubject());
    // FIXME assertEquals(0, mime.getSize());
  }

  @Test
  public void testSendEmailWithAutentication() throws Exception {
    final Map<String, Object> parameters = getBasicSettings();
    parameters.put("userName", "john");
    parameters.put("password", "doe");
    executeConnector(parameters);
    final List<WiserMessage> messages = server.getMessages();
    // FIXME assertEquals(1, messages.size());
    final WiserMessage message = messages.get(0);
    // FIXME assertNotNull(message.getEnvelopeSender());
    // FIXME assertEquals(ADDRESSJOHN, message.getEnvelopeReceiver());
    final MimeMessage mime = message.getMimeMessage();
    // FIXME assertEquals(MAIL_SUBJECT, mime.getSubject());
    // FIXME assertEquals(0, mime.getSize());
  }

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
    // FIXME assertEquals(2, messages.size());
    WiserMessage message = messages.get(0);
    // FIXME assertEquals(ADDRESSMARK, message.getEnvelopeSender());
    // FIXME assertEquals(ADDRESSJOHN, message.getEnvelopeReceiver());
    MimeMessage mime = message.getMimeMessage();
    // FIXME assertEquals(MAIL_SUBJECT, mime.getSubject());
    // FIXME assertEquals(0, mime.getSize());

    message = messages.get(1);
    // FIXME assertEquals(ADDRESSMARK, message.getEnvelopeSender());
    // FIXME assertEquals(ADDRESSPATTY, message.getEnvelopeReceiver());
    mime = message.getMimeMessage();
    // FIXME assertEquals(MAIL_SUBJECT, mime.getSubject());
    // FIXME assertEquals(0, mime.getSize());
  }

  @Test
  public void sendEmailWithCcRecipientsAddresses() throws Exception {
    final Map<String, Object> parameters = getBasicSettings();
    parameters.put("cc", ADDRESSPATTY);
    parameters.put("from", ADDRESSMARK);
    executeConnector(parameters);

    List<WiserMessage> messages = server.getMessages();
    //    // FIXME assertEquals(2, messages.size());
    assertThat(messages.size()).isEqualTo(2);
    WiserMessage message = messages.get(0);
    assertThat(message.getEnvelopeSender()).isEqualTo(ADDRESSMARK);
    assertThat(message.getEnvelopeReceiver()).isEqualTo(ADDRESSJOHN);
    //    // FIXME assertEquals(ADDRESSMARK, message.getEnvelopeSender());
    //    // FIXME assertEquals(ADDRESSJOHN, message.getEnvelopeReceiver());
    MimeMessage mime = message.getMimeMessage();
    //    // FIXME assertEquals(MAIL_SUBJECT, mime.getSubject());
    assertThat(mime.getSubject()).isEqualTo(MAIL_SUBJECT);
    //    // FIXME assertEquals(0, mime.getSize());
    assertThat(mime.getSize()).isEqualTo(0);

    message = messages.get(1);
    assertThat(message.getEnvelopeSender()).isEqualTo(ADDRESSMARK);
    assertThat(message.getEnvelopeReceiver()).isEqualTo(ADDRESSPATTY);
    //    // FIXME assertEquals(ADDRESSMARK, message.getEnvelopeSender());
    //    // FIXME assertEquals(ADDRESSPATTY, message.getEnvelopeReceiver());
    mime = message.getMimeMessage();
    //    // FIXME assertEquals(MAIL_SUBJECT, mime.getSubject());
    assertThat(mime.getSubject()).isEqualTo(MAIL_SUBJECT);
    assertThat(mime.getSize()).isEqualTo(0);
    //    // FIXME assertEquals(0, mime.getSize());

    parameters.put("cc", ADDRESSPATTY + ", " + ADDRESSMARK);
    executeConnector(parameters);
    messages = server.getMessages();

    assertThat(messages.size()).isEqualTo(5);
    //    // FIXME assertEquals(5, messages.size());
    message = messages.get(4);
    assertThat(message.getEnvelopeSender()).isEqualTo(ADDRESSMARK);
    assertThat(message.getEnvelopeReceiver()).isEqualTo(ADDRESSMARK);
    //    // FIXME assertEquals(ADDRESSMARK, message.getEnvelopeSender());
    //    // FIXME assertEquals(ADDRESSMARK, message.getEnvelopeReceiver());
    mime = message.getMimeMessage();
    assertThat(mime.getSubject()).isEqualTo(MAIL_SUBJECT);
    assertThat(mime.getSize()).isEqualTo(0);
    //    // FIXME assertEquals(MAIL_SUBJECT, mime.getSubject());
    //    // FIXME assertEquals(0, mime.getSize());
  }

  @Test
  public void sendEmailWithReturnPathAddress() throws Exception {
    final Map<String, Object> parameters = getBasicSettings();
    parameters.put("returnPath", ADDRESSPATTY);
    parameters.put("from", ADDRESSMARK);
    executeConnector(parameters);

    List<WiserMessage> messages = server.getMessages();
    // FIXME assertEquals(1, messages.size());
    WiserMessage message = messages.get(0);
    // FIXME assertEquals(ADDRESSPATTY, message.getEnvelopeSender());
    // FIXME assertEquals(new InternetAddress(ADDRESSMARK), message.getMimeMessage().getFrom()[0]);
    // FIXME assertEquals(ADDRESSJOHN, message.getEnvelopeReceiver());
  }

  @Test
  public void sendEmailWithPlainMessage() throws Exception {
    final Map<String, Object> parameters = getBasicSettings();
    parameters.put("messageTemplate", PLAIN_MESSAGE);
    parameters.put("from", ADDRESSMARK);
    executeConnector(parameters);

    final List<WiserMessage> messages = server.getMessages();
    // FIXME assertEquals(1, messages.size());
    final WiserMessage message = messages.get(0);
    // FIXME assertEquals(ADDRESSMARK, message.getEnvelopeSender());
    // FIXME assertEquals(ADDRESSJOHN, message.getEnvelopeReceiver());
    final MimeMessage mime = message.getMimeMessage();
    // FIXME assertEquals(MAIL_SUBJECT, mime.getSubject());
    // FIXME assertTrue(mime.getContentType().contains(TEXT_PLAIN));
    // FIXME assertEquals(PLAIN_MESSAGE, mime.getContent());
  }

  @Test
  public void sendEmailWithReplacements() throws Exception {
    // given
    final Map<String, Object> parameters = getBasicSettings();
    final List<List<Object>> replacements =
        Collections.singletonList(Arrays.asList("customer", (Object) "Walter Bates"));
    parameters.put("html", false);
    parameters.put("messageTemplate", "Dear ${customer}");
    parameters.put("replacements", replacements);
    parameters.put("from", ADDRESSMARK);

    // when
    executeConnector(parameters);

    // then
    final List<WiserMessage> messages = server.getMessages();
    // FIXME assertEquals(1, messages.size());
    final WiserMessage message = messages.get(0);
    // FIXME assertEquals(ADDRESSMARK, message.getEnvelopeSender());
    // FIXME assertEquals(ADDRESSJOHN, message.getEnvelopeReceiver());
    final MimeMessage mime = message.getMimeMessage();
    // FIXME assertEquals(MAIL_SUBJECT, mime.getSubject());
    // FIXME assertTrue(mime.getContentType().contains(TEXT_PLAIN));
    // FIXME assertEquals("Dear Walter Bates", mime.getContent());
  }

  @Test
  public void sendEmailWithEmptyReplacements() throws Exception {
    // given
    final Map<String, Object> parameters = getBasicSettings();
    final List<List<Object>> replacements =
        Collections.singletonList(Arrays.asList("customer", (Object) null));
    parameters.put("html", false);
    parameters.put("messageTemplate", "Dear ${customer}");
    parameters.put("replacements", replacements);
    parameters.put("from", ADDRESSMARK);

    // when
    executeConnector(parameters);

    // then
    final List<WiserMessage> messages = server.getMessages();
    // FIXME assertEquals(1, messages.size());
    final WiserMessage message = messages.get(0);
    // FIXME assertEquals(ADDRESSMARK, message.getEnvelopeSender());
    // FIXME assertEquals(ADDRESSJOHN, message.getEnvelopeReceiver());
    final MimeMessage mime = message.getMimeMessage();
    // FIXME assertEquals(MAIL_SUBJECT, mime.getSubject());
    // FIXME assertTrue(mime.getContentType().contains(TEXT_PLAIN));
    // FIXME assertEquals("Dear ", mime.getContent());
  }

  @Test
  public void sendHtmlEmailWithReplacements() throws Exception {
    // given
    final Map<String, Object> parameters = getBasicSettings();
    parameters.put("html", true);
    parameters.put("messageTemplate", "<p>Dear ${customer}</p>");
    final List<List<Object>> replacements =
        Collections.singletonList(Arrays.asList("customer", (Object) "Walter Bates"));
    parameters.put("replacements", replacements);
    parameters.put("from", ADDRESSMARK);

    // when
    executeConnector(parameters);

    // then
    final List<WiserMessage> messages = server.getMessages();
    // FIXME assertEquals(1, messages.size());
    final WiserMessage message = messages.get(0);
    // FIXME assertEquals(ADDRESSMARK, message.getEnvelopeSender());
    // FIXME assertEquals(ADDRESSJOHN, message.getEnvelopeReceiver());
    final MimeMessage mime = message.getMimeMessage();
    // FIXME assertEquals(MAIL_SUBJECT, mime.getSubject());
    // FIXME assertTrue(mime.getContentType().contains(TEXT_HTML));
    // FIXME assertEquals("<p>Dear Walter Bates</p>", mime.getContent());
  }

  @Test
  public void sendEmailWithHtmlMessage() throws Exception {
    final Map<String, Object> parameters = getBasicSettings();
    parameters.put("html", true);
    parameters.put("messageTemplate", HTML_MESSAGE);
    parameters.put("from", ADDRESSMARK);
    executeConnector(parameters);

    final List<WiserMessage> messages = server.getMessages();
    // FIXME assertEquals(1, messages.size());
    final WiserMessage message = messages.get(0);
    // FIXME assertEquals(ADDRESSMARK, message.getEnvelopeSender());
    // FIXME assertEquals(ADDRESSJOHN, message.getEnvelopeReceiver());
    final MimeMessage mime = message.getMimeMessage();
    // FIXME assertEquals(MAIL_SUBJECT, mime.getSubject());
    // FIXME assertTrue(mime.getContentType().contains(TEXT_HTML));
    // FIXME assertEquals(HTML_MESSAGE, mime.getContent());
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
    // FIXME assertEquals(1, messages.size());
    final WiserMessage message = messages.get(0);
    // FIXME assertEquals("alice@bob.charly", message.getEnvelopeSender());
    // FIXME assertEquals(ADDRESSJOHN, message.getEnvelopeReceiver());
    final MimeMessage mime = message.getMimeMessage();
    // FIXME assertEquals(MAIL_SUBJECT, mime.getSubject());
    // FIXME assertEquals(0, mime.getSize());
    // FIXME assertEquals("Bonita Mailer", mime.getHeader("X-Mailer", ""));
    // FIXME assertEquals("2 (High)", mime.getHeader("X-Priority", ""));
    // FIXME assertEquals("anyValue", mime.getHeader("WhatIWant", ""));
    // FIXME assertNotSame("alice@bob.charly", mime.getHeader("From"));
    // FIXME assertFalse(mime.getContentType().contains("video/mpeg"));
    // FIXME assertNotSame("IWantToHackTheServer", mime.getHeader("Message-ID"));
  }

  @Test
  public void sendCyrillicEmail() throws Exception {
    final Map<String, Object> parameters = getBasicSettings();
    parameters.put("subject", CYRILLIC_SUBJECT);
    parameters.put("messageTemplate", CYRILLIC_MESSAGE);
    executeConnector(parameters);

    final List<WiserMessage> messages = server.getMessages();
    // FIXME assertEquals(1, messages.size());
    final WiserMessage message = messages.get(0);
    // FIXME assertNotNull(message.getEnvelopeSender());
    // FIXME assertEquals(ADDRESSJOHN, message.getEnvelopeReceiver());
    final MimeMessage mime = message.getMimeMessage();
    // FIXME assertEquals(CYRILLIC_SUBJECT, mime.getSubject());
    // FIXME assertTrue(mime.getContentType().contains(TEXT_PLAIN));
    // FIXME assertEquals(CYRILLIC_MESSAGE, mime.getContent());
  }

  @Test
  public void sendBadEncodingCyrillicEmail() throws Exception {
    final Map<String, Object> parameters = getBasicSettings();
    parameters.put("charset", "iso-8859-1");
    parameters.put("messageTemplate", CYRILLIC_MESSAGE);
    executeConnector(parameters);

    final List<WiserMessage> messages = server.getMessages();
    // FIXME assertEquals(1, messages.size());
    final WiserMessage message = messages.get(0);
    // FIXME assertNotNull(message.getEnvelopeSender());
    // FIXME assertEquals(ADDRESSJOHN, message.getEnvelopeReceiver());
    final MimeMessage mime = message.getMimeMessage();
    // FIXME assertEquals(MAIL_SUBJECT, mime.getSubject());
    // FIXME assertTrue(mime.getContentType().contains(TEXT_PLAIN));
    // FIXME assertEquals("? ? ?", mime.getContent());
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
    // FIXME assumeNotNull(messages);
    assertThat(
            ((MimeMultipart) messages.get(0).getMimeMessage().getContent())
                .getBodyPart(1)
                .getFileName())
        .isEqualTo("filename.txt");
    // BS-11239 : change multipart mime type in order to have attachments openable on iPhone
    assertThat(messages.get(0).getMimeMessage().getContentType()).startsWith("multipart/mixed;");
  }

  @Test
  public void sendFileDocumentWithSpecialEncoding()
      throws BonitaException, MessagingException, IOException {
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
    // FIXME assumeNotNull(messages);
    assertThat(
            ((MimeMultipart) messages.get(0).getMimeMessage().getContent())
                .getBodyPart(1)
                .getFileName())
        .isEqualTo(MimeUtility.encodeText("最日本最.TXT"));
  }

  @Test
  public void sendWithDocumentList() throws BonitaException, MessagingException, IOException {
    DocumentImpl document1 = createDocument(1L, "toto1");
    DocumentImpl document2 = createDocument(2L, "toto2");
    List<Document> documents = Arrays.<Document>asList(document1, document2);
    Map<String, Object> parameters = getBasicSettings();
    parameters.put(EmailConnector.ATTACHMENTS, documents);

    executeConnector(parameters);

    List<WiserMessage> messages = server.getMessages();
    // FIXME assumeNotNull(messages);
    List<byte[]> contents =
        getAttachmentsContent((MimeMultipart) messages.get(0).getMimeMessage().getContent());
    assertThat(new String(contents.get(1))).isEqualTo("toto1");
    assertThat(new String(contents.get(2))).isEqualTo("toto2");
  }

  @Test
  public void sendWithMultipleDocumentList()
      throws BonitaException, MessagingException, IOException {
    DocumentImpl document1 = createDocument(1L, "toto1");
    DocumentImpl document2 = createDocument(2L, "toto2");
    DocumentImpl document3 = createDocument(3L, "toto3");
    DocumentImpl document4 = createDocument(4L, "toto4");
    List<Document> documents1 = Arrays.<Document>asList(document1, document2);
    List<Document> documents2 = Arrays.<Document>asList(document3, document4);
    List lists = Arrays.asList(documents1, documents2);
    Map<String, Object> parameters = getBasicSettings();
    parameters.put(EmailConnector.ATTACHMENTS, lists);

    executeConnector(parameters);

    List<WiserMessage> messages = server.getMessages();
    // FIXME assumeNotNull(messages);
    List<byte[]> contents =
        getAttachmentsContent((MimeMultipart) messages.get(0).getMimeMessage().getContent());
    assertThat(new String(contents.get(1))).isEqualTo("toto1");
    assertThat(new String(contents.get(2))).isEqualTo("toto2");
    assertThat(new String(contents.get(3))).isEqualTo("toto3");
    assertThat(new String(contents.get(4))).isEqualTo("toto4");
  }

  private List<byte[]> getAttachmentsContent(MimeMultipart multipart)
      throws MessagingException, IOException {
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
    parameters.put(EmailConnector.MESSAGE_TEMPLATE, "Hello Mr message\n This is an email content");
    List<String> attachments = Collections.singletonList("Document1");
    parameters.put(EmailConnector.ATTACHMENTS, attachments);

    executeConnector(parameters);

    List<WiserMessage> messages = server.getMessages();
    // FIXME assumeNotNull(messages);
    assertThat(
            (String)
                ((MimeMultipart) messages.get(0).getMimeMessage().getContent())
                    .getBodyPart(0)
                    .getContent())
        .contains("http://www.bonitasoft.com");
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
    parameters.put(EmailConnector.MESSAGE_TEMPLATE, "Hello Mr message\n This is an email content");
    List<String> attachments = Collections.singletonList("Document1");
    parameters.put(EmailConnector.ATTACHMENTS, attachments);

    executeConnector(parameters);

    List<WiserMessage> messages = server.getMessages();
    // FIXME assumeNotNull(messages);
    assertThat(
            (String)
                ((MimeMultipart) messages.get(0).getMimeMessage().getContent())
                    .getBodyPart(0)
                    .getContent())
        .doesNotContain("Document1");
  }

  @Test
  public void shouldReplaceSubject() throws BonitaException, MessagingException, IOException {
    Map<String, Object> parameters = getBasicSettings();
    parameters.put(EmailConnector.SUBJECT, "your case ${caseId}");
    parameters.put(EmailConnector.HTML, true);
    parameters.put(EmailConnector.MESSAGE_TEMPLATE, "<p>Dear ${customer}</p>");
    List<List<Object>> replacements = new ArrayList<List<Object>>();
    replacements.add(Arrays.asList("customer", (Object) "Walter Bates"));
    replacements.add(Arrays.asList("caseId", (Object) 123456L));
    parameters.put(EmailConnector.REPLACEMENTS, replacements);

    executeConnector(parameters);

    List<WiserMessage> messages = server.getMessages();
    assertThat(messages).isNotEmpty();
    assertThat(messages.get(0).getMimeMessage().getContent()).isEqualTo("<p>Dear Walter Bates</p>");
    assertThat(messages.get(0).getMimeMessage().getSubject()).isEqualTo("your case 123456");
  }
}
