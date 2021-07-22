package org.bonitasoft.connectors.email.templating.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bonitasoft.connectors.email.templating.EmailConnector;
import org.bonitasoft.engine.connector.ConnectorValidationException;
import org.bonitasoft.engine.exception.BonitaException;
import org.junit.Test;

public class EmailConnectorValidationTest {

  private static final String SMTP_HOST = "localhost";

  private static final String ADDRESSJOHN = "john.doe@bonita.org";

  private static final String SUBJECT = "Testing EmailConnector";

  private static final String PLAINMESSAGE = "Plain Message";

  private void validateConnector(final Map<String, Object> parameters)
      throws ConnectorValidationException {
    final EmailConnector email = new EmailConnector();
    email.setInputParameters(parameters);
    email.validateInputParameters();
  }

  private Map<String, Object> getBasicSettings() {
    final Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("smtpHost", SMTP_HOST);
    parameters.put("smtpPort", 0);
    parameters.put("to", ADDRESSJOHN);
    parameters.put("subject", SUBJECT);
    parameters.put("sslSupport", false);
    parameters.put("html", false);
    return parameters;
  }

  @Test
  public void validatesSimpliestEmail() throws ConnectorValidationException {
    validateConnector(getBasicSettings());
  }

  @Test
  public void validatesEmailWithAValidFromAddress() throws ConnectorValidationException {
    final Map<String, Object> parameters = getBasicSettings();
    parameters.put("from", "john@bpm.com");
    validateConnector(parameters);
  }

  @Test
  public void validatesEmailWithANullValidFromAddress() throws ConnectorValidationException {
    final Map<String, Object> parameters = getBasicSettings();
    parameters.put("from", null);
    validateConnector(parameters);
  }

  @Test(expected = ConnectorValidationException.class)
  public void thowsExceptionDueToInvalidEmailAddressFrom() throws ConnectorValidationException {
    final Map<String, Object> parameters = getBasicSettings();
    parameters.put("from", "@bonita.org");
    validateConnector(parameters);
  }

  @Test(expected = ConnectorValidationException.class)
  public void thowsExceptionDueToNoRecipientAddress() throws ConnectorValidationException {
    final Map<String, Object> parameters = getBasicSettings();
    parameters.remove("to");
    validateConnector(parameters);
  }

  @Test
  public void validEmailEvenIfHeadersAreNull() throws ConnectorValidationException {
    final Map<String, Object> parameters = getBasicSettings();
    parameters.put("headers", null);
    validateConnector(parameters);
  }

  @Test
  public void validEmailWithExtraHeaders() throws ConnectorValidationException {
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
  public void validEmailWithANullMessage() throws ConnectorValidationException {
    final Map<String, Object> parameters = getBasicSettings();
    parameters.put("message", null);
    validateConnector(parameters);
  }

  @Test
  public void validEmailWithAEmptyMessage() throws ConnectorValidationException {
    final Map<String, Object> parameters = getBasicSettings();
    parameters.put("message", "");
    validateConnector(parameters);
  }

  @Test
  public void validEmailWithAMessage() throws ConnectorValidationException {
    final Map<String, Object> parameters = getBasicSettings();
    parameters.put("message", PLAINMESSAGE);
    validateConnector(parameters);
  }

  @Test
  public void validAuthentication() throws ConnectorValidationException {
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

  @Test(expected = ConnectorValidationException.class)
  public void throwsExceptionWhenSmtpHostIsNull() throws ConnectorValidationException {
    final Map<String, Object> parameters = getBasicSettings();
    parameters.put("smtpHost", null);
    validateConnector(parameters);
  }

  @Test(expected = ConnectorValidationException.class)
  public void throwsExceptionWhenSmtpPortIsNull() throws ConnectorValidationException {
    final Map<String, Object> parameters = getBasicSettings();
    parameters.put("smtpPort", null);
    validateConnector(parameters);
  }

  @Test(expected = ConnectorValidationException.class)
  public void throwsExceptionWhenWrappedSmtpPortIsLessThanRange() throws BonitaException {
    final Map<String, Object> parameters = getBasicSettings();
    parameters.put("smtpPort", -1);
    validateConnector(parameters);
  }

  @Test(expected = ConnectorValidationException.class)
  public void throwsExceptionWhenWrappedSmtpPortWithGreaterThanRange() throws BonitaException {
    final Map<String, Object> parameters = getBasicSettings();
    parameters.put("smtpPort", 65536);
    validateConnector(parameters);
  }

  @Test(expected = ConnectorValidationException.class)
  public void throwsExceptionWhenSmtpPortIsLessThanRange() throws BonitaException {
    final Map<String, Object> parameters = getBasicSettings();
    parameters.put("smtpPort", -1);
    validateConnector(parameters);
  }

  @Test(expected = ConnectorValidationException.class)
  public void throwsExceptionWhenSmtpPortWithGreaterThanRange() throws BonitaException {
    final Map<String, Object> parameters = getBasicSettings();
    parameters.put("smtpPort", 65536);
    validateConnector(parameters);
  }

  @Test
  public void validEmailWithANullSubject() throws BonitaException {
    final Map<String, Object> parameters = getBasicSettings();
    parameters.put("subject", null);
    validateConnector(parameters);
  }

  @Test
  public void validEmailWithASubject() throws BonitaException {
    final Map<String, Object> parameters = getBasicSettings();
    parameters.put("subject", SUBJECT);
    validateConnector(parameters);
  }

  //
  // public void testSetNullSsl() throws BonitaException {
  // email = getBasicSettings();
  // email.setSslSupport(null);
  // assertTrue(email.validate().isEmpty());
  // assertFalse(email.isSslSupport());
  // email = null;
  // }
  //
  // public void testSetSsl() {
  // email = getBasicSettings();
  // email.setSslSupport(new Boolean("true"));
  // assertTrue(email.isSslSupport());
  // email.setSslSupport(new Boolean("True"));
  // assertTrue(email.isSslSupport());
  // email.setSslSupport(new Boolean(""));
  // assertFalse(email.isSslSupport());
  // email.setSslSupport(new Boolean("false"));
  // assertFalse(email.isSslSupport());
  // email.setSslSupport(new Boolean("whatIwant"));
  // assertFalse(email.isSslSupport());
  // }
  //
  // public void testSetNullStarttlsSupport() throws BonitaException {
  // email = getBasicSettings();
  // email.setStarttlsSupport(null);
  // assertTrue(email.validate().isEmpty());
  // assertFalse(email.isSslSupport());
  // email = null;
  // }
  //
  // public void testSetNullAttachments() throws BonitaException {
  // email = getBasicSettings();
  // final List<Object> attachments = null;
  // email.setAttachments(attachments);
  // assertTrue(email.validate().isEmpty());
  // assertTrue(email.getAttachments().isEmpty());
  // email = null;
  // }
  //
  // public void testSetAttachments() {
  // email = getBasicSettings();
  // final List<Object> attachments = new ArrayList<Object>();
  // attachments.add("../test/test0.xml");
  // attachments.add("../test/test1.xml");
  // attachments.add("../test/test2.xml");
  // email.setAttachments(attachments);
  // assertNotNull(email.getAttachments());
  // assertEquals(3, email.getAttachments().size());
  // email = null;
  // }
  //
  // public void testSetTwiceAttachments() {
  // email = getBasicSettings();
  // final List<Object> attachments = new ArrayList<Object>();
  // attachments.add("../test/test0.xml");
  // attachments.add("../test/test1.xml");
  // attachments.add("../test/test2.xml");
  // email.setAttachments(attachments);
  // assertNotNull(email.getAttachments());
  // assertEquals(3, email.getAttachments().size());
  // attachments.clear();
  // email.setAttachments(attachments);
  // assertNotNull(email.getAttachments());
  // assertTrue(email.getAttachments().isEmpty());
  // }
  // public void testSetNullImages() throws BonitaException {
  // email = getBasicSettings();
  // final Map<String, String> images = null;
  // email.setImages(images);
  // assertTrue(email.validate().isEmpty());
  // assertTrue(email.getImages().isEmpty());
  // email = null;
  // }
  //
  // public void testSetImagesWithAnEmptyString() throws BonitaException {
  // email = getBasicSettings();
  // final Map<String, String> images = new HashMap<String, String>();
  // email.setImages(images);
  // assertTrue(email.validate().isEmpty());
  // assertTrue(email.getImages().isEmpty());
  // email = null;
  // }
  //
  // public void testSetImagesTwice() throws BonitaException {
  // email = getBasicSettings();
  // final Map<String, String> firstSet = new HashMap<String, String>();
  // firstSet.put("path_one", "firstAlias");
  // firstSet.put("path_two", "secondAlias");
  // firstSet.put("path_three", "thirdAlias");
  // email.setImages(firstSet);
  // assertTrue(email.validate().isEmpty());
  // assertEquals(firstSet, email.getImages());
  // final Map<String, String> secondSet = new HashMap<String, String>();
  // secondSet.put("path1", "Alias A");
  // secondSet.put("path2", "Alias B");
  // email.setImages(secondSet);
  // assertTrue(email.validate().isEmpty());
  // assertEquals(secondSet, email.getImages());
  // }

}
