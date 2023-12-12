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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.document.Document;
import org.bonitasoft.engine.bpm.document.DocumentNotFoundException;
import org.bonitasoft.engine.connector.AbstractConnector;
import org.bonitasoft.engine.connector.ConnectorException;
import org.bonitasoft.engine.connector.ConnectorValidationException;

/**
 * This connector provides an email sending service.
 * 
 * @author Matthieu Chaffotte
 * @author Yanyan Liu
 * @author Baptiste Mesta
 */
public class EmailConnector extends AbstractConnector {

    /**
     * The files to attach to the email.
     */
    public static final String ATTACHMENTS = "attachments";

    /**
     * The extra header fields of the email.
     */
    public static final String HEADERS = "headers";

    /**
     * Charset of the message
     */
    public static final String CHARSET = "charset";

    /**
     * The message content of the email.
     */
    public static final String MESSAGE = "message";

    /**
     * Indicates whether the content of the email is in HTML format.
     */
    public static final String HTML = "html";

    /**
     * The subject of the email.
     */
    public static final String SUBJECT = "subject";

    /**
     * The "bcc" recipient(s) email address(es).
     */
    public static final String BCC = "bcc";

    /**
     * The "cc" recipient(s) email address(es).
     */
    public static final String CC = "cc";

    /**
     * The "to" recipient(s) email address(es).
     */
    public static final String TO = "to";

    /**
     * The "Reply-to" recipient(s) email address(es).
     */
    public static final String REPLY_TO = "replyTo";

    /**
     * The sender's email address.
     */
    public static final String FROM = "from";

    /**
     * The return-path email address.
     */
    public static final String RETURN_PATH = "returnPath";

    /**
     * The password used for authentication.
     */
    public static final String PASSWORD = "password";

    /**
     * The user name used for authentication.
     */
    public static final String USER_NAME = "userName";

    /**
     * Indicates whether the SMTP server uses a STARTTLS support.
     */
    public static final String STARTTLS_SUPPORT = "starttlsSupport";

    /**
     * Indicates whether the SMTP server uses an SSL support.
     */
    public static final String SSL_SUPPORT = "sslSupport";

    public static final String SMTP_PORT = "smtpPort";

    /**
     * The name or the IP address of the SMTP server.
     */
    public static final String SMTP_HOST = "smtpHost";

    /**
     * Indicates whether the SMTP server certificate should be always trusted
     */
    public static final String TRUST_CERTIFICATE = "trustCertificate";

    private Logger logger = Logger.getLogger(this.getClass().getName());

    @Override
    public void validateInputParameters() throws ConnectorValidationException {
        logInputParameters();
        List<String> errors = new ArrayList<>(1);
        final Integer smtpPort = (Integer) getInputParameter(SMTP_PORT);

        if (smtpPort == null) {
            errors.add("smtpPort cannot be null!");
        } else {
            if (smtpPort < 0) {
                errors.add("smtpPort cannot be less than 0!");
            } else if (smtpPort > 65535) {
                errors.add("smtpPort cannot be greater than 65535!");
            }
        }

        final String smtpHost = (String) getInputParameter(SMTP_HOST);
        if (smtpHost == null) {
            errors.add("smtpHost cannot be null!");
        }

        final String from = (String) getInputParameter(FROM);
        checkInputParameter(from, errors);

        final String returnPath = (String) getInputParameter(RETURN_PATH);
        checkInputParameter(returnPath, errors);

        final String to = (String) getInputParameter(TO);
        checkInputParameter(to, errors);

        final String replyTo = (String) getInputParameter(REPLY_TO);
        checkInputParameter(replyTo, errors);

        final String cc = (String) getInputParameter(CC);
        checkInputParameter(cc, errors);

        final String bcc = (String) getInputParameter(BCC);
        checkInputParameter(bcc, errors);

        if (to == null && cc == null && bcc == null) {
            errors.add("No recipient address(es) is set (either in 'to', 'cc' or 'bcc'");
        }
        if (!errors.isEmpty()) {
            throw new ConnectorValidationException(this, errors);
        }
    }

    private void logInputParameters() {
        if (!logger.isLoggable(Level.INFO)) {
            return;
        }
        logInputParameter(CHARSET);
        logInputParameter(MESSAGE);
        logInputParameter(HTML);
        logInputParameter(SUBJECT);
        logInputParameter(BCC);
        logInputParameter(CC);
        logInputParameter(TO);
        logInputParameter(FROM);
        logInputParameter(RETURN_PATH);
        logInputParameter(USER_NAME);
        logInputParameter(STARTTLS_SUPPORT);
        logInputParameter(SSL_SUPPORT);
        logInputParameter(TRUST_CERTIFICATE);
        logInputParameter(SMTP_PORT);
        logInputParameter(SMTP_HOST);
        logInputParameter(REPLY_TO);

        logger.info(PASSWORD + " ******");
        @SuppressWarnings("unchecked")
        List<Object> attachments = (List<Object>) getInputParameter(ATTACHMENTS);

        if (attachments == null) {
            logger.info("Attachments null");
        } else {
            for (Object attachment : attachments) {
                logger.info(() -> "Attachment " + attachment);
            }
        }

        Map<String, String> headers = getHeaders();
        if (headers.isEmpty()) {
            logger.info("Headers null");
        } else {
            for (Entry<String, String> header : headers.entrySet()) {
                logger.info("Header " + header.getKey() + " " + header.getValue());
            }
        }
        logInputParameter(HEADERS);
    }

    private void logInputParameter(String parameterName) {
        logger.info(() -> parameterName + " " + getInputParameter(parameterName));
    }

    private void checkInputParameter(String parameter, List<String> errors) {
        if (parameter != null && !parameter.isEmpty() && !checkAddresses(parameter)) {
            errors.add(parameter + " address in invalid");
        }
    }

    private boolean checkAddresses(final String addresses) {
        try {
            InternetAddress.parse(addresses);
        } catch (final AddressException e) {
            return false;
        }
        return true;
    }

    /**
     * Returns an unshared email session from the SMTP server's properties.
     *
     * @return an unshared email session from the SMTP server's properties
     */
     Session getSession() {
        final Properties properties = new Properties();
        properties.put("mail.smtp.host", getInputParameter(SMTP_HOST));
        final String smtpPort = String.valueOf(getInputParameter(SMTP_PORT));
        properties.put("mail.smtp.port", smtpPort);
        String returnPath = (String) getInputParameter(RETURN_PATH);
        if (returnPath == null || returnPath.isEmpty()) {
            returnPath = (String) getInputParameter(FROM);
        }
        if (returnPath != null && !returnPath.isEmpty()) {
            properties.put("mail.smtp.from", returnPath);
        }
       
        Boolean startTlsParameter = (Boolean) getInputParameter(STARTTLS_SUPPORT, false);
        Boolean sslParameter = (Boolean) getInputParameter(SSL_SUPPORT, true);
        // Using STARTTLS
        boolean useStarttls = Boolean.TRUE.equals(startTlsParameter);
        if (useStarttls) {
            properties.put("mail.smtp.starttls.enable", "true");
            properties.put("mail.smtp.starttls.required", "true");
        }
        // Using SSL
        boolean useSSL = Boolean.TRUE.equals(sslParameter);
        if(useSSL && Boolean.FALSE.equals(startTlsParameter)) {
            properties.put("mail.smtp.ssl.enable", "true");
        }
        if(useSSL || useStarttls) {
            if((boolean)getInputParameter(TRUST_CERTIFICATE, false)) {
                properties.put("mail.smtp.ssl.trust", "*");
            }else {
                properties.put("mail.smtp.ssl.checkserveridentity", "true");
            }
        }
        Session session;
        final String username = (String) getInputParameter(USER_NAME);
        final String password = (String) getInputParameter(PASSWORD);
        if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
            properties.put("mail.smtp.auth", "true");
            final Authenticator authenticator = new SMTPAuthenticator(username, password);
            session = Session.getInstance(properties, authenticator);
        } else {
            session = Session.getInstance(properties, null);
        }
        return session;
    }

    private Map<String, String> getHeaders() {
        @SuppressWarnings("unchecked")
        final List<List<Object>> headersList = (List<List<Object>>) getInputParameter(HEADERS);
        final Map<String, String> headers = new HashMap<>();
        if (headersList != null) {
            for (List<Object> rows : headersList) {
                if (rows.size() == 2) {
                    Object keyContent = rows.get(0);
                    Object valueContent = rows.get(1);
                    if (keyContent != null && valueContent != null) {
                        final String key = keyContent.toString();
                        final String value = valueContent.toString();
                        headers.put(key, value);
                    }
                }
            }
        }
        return headers;
    }

    /**
     * Get a MimeMessage from email properties.
     *
     * @param emailSession
     *        the email session
     * @throws AddressException
     *         if an exception occurs
     */
    private MimeMessage getEmail(Session emailSession) throws ConnectorException {
        MimeMessage mimeMessage = new MimeMessage(emailSession);
        try {
            setMessageAddresses(mimeMessage);
            setMessageContent(mimeMessage);
            mimeMessage.setSentDate(new Date());
        } catch (MessagingException me) {
            throw new ConnectorException(me.getMessage(), me.getCause());
        }
        return mimeMessage;
    }

    private void setMessageContent(MimeMessage mimeMessage) throws MessagingException, ConnectorException {
        String subject = (String) getInputParameter(SUBJECT);
        String charset = (String) getInputParameter(CHARSET, "UTF-8");
        String message = (String) getInputParameter(MESSAGE, "");
        @SuppressWarnings("unchecked")
        List<Object> attachments = (List<Object>) getInputParameter(ATTACHMENTS);
        boolean html = (Boolean) getInputParameter(HTML, true);

        mimeMessage.setSubject(subject, charset);

        for (Map.Entry<String, String> h : getHeaders().entrySet()) {
            if (h.getKey() != null && h.getValue() != null && !h.getKey().equals("Content-ID")) {
                mimeMessage.setHeader(h.getKey(), h.getValue());
            }
        }

        if (attachments != null) {
            final Multipart multipart = getMultipart(html, message, charset, attachments);
            mimeMessage.setContent(multipart);
        } else {
            if (html) {
                mimeMessage.setText(message, charset, HTML);
            } else {
                mimeMessage.setText(message, charset);
            }
        }
    }

    private void setMessageAddresses(MimeMessage mimeMessage) throws MessagingException {
        String from = (String) getInputParameter(FROM);
        String to = (String) getInputParameter(TO);
        String cc = (String) getInputParameter(CC);
        String bcc = (String) getInputParameter(BCC);
        String replyTo = (String) getInputParameter(REPLY_TO);

        if (from != null && !from.isEmpty()) {
            mimeMessage.setFrom(new InternetAddress(from));
        } else {
            mimeMessage.setFrom();
        }
        mimeMessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false));
        if (cc != null && !cc.isEmpty()) {
            mimeMessage.setRecipients(Message.RecipientType.CC, InternetAddress.parse(cc, false));
        }
        if (bcc != null && !bcc.isEmpty()) {
            mimeMessage.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(bcc, false));
        }
        if (replyTo != null && !replyTo.isEmpty()) {
            mimeMessage.setReplyTo(InternetAddress.parse(replyTo));
        }
    }

    /**
     * Get the <code>Multipart</code> of the email.
     */
    private Multipart getMultipart(final boolean html, final String message, final String charset,
            List<Object> attachments)
            throws ConnectorException {
        try {
            StringBuilder messageBody = new StringBuilder(message);
            ProcessAPI processAPI = getAPIAccessor().getProcessAPI();
            final Multipart body = new MimeMultipart("mixed");
            List<MimeBodyPart> bodyParts = new ArrayList<>();
            if (attachments != null) {
                for (Object attachment : attachments) {
                    handleAttachment(html, messageBody, processAPI, bodyParts, attachment);
                }
            }
            MimeBodyPart bodyPart = new MimeBodyPart();
            if (html) {
                bodyPart.setText(messageBody.toString(), charset, HTML);
            } else {
                bodyPart.setText(messageBody.toString(), charset);
            }
            body.addBodyPart(bodyPart);

            for (MimeBodyPart part : bodyParts) {
                body.addBodyPart(part);
            }
            return body;
        } catch (ConnectorException e) {
            throw e;
        } catch (Exception e) {
            throw new ConnectorException("unable to retrieve attachments for the email", e);
        }

    }

    @SuppressWarnings("rawtypes")
    private void handleAttachment(boolean html, StringBuilder messageBody, ProcessAPI processAPI,
            List<MimeBodyPart> bodyParts, Object attachment)
            throws ConnectorException, DocumentNotFoundException, MessagingException, UnsupportedEncodingException {
        if (attachment instanceof List) {
            for (Object subAttachment : ((List) attachment)) {
                handleAttachment(html, messageBody, processAPI, bodyParts, subAttachment);
            }
            return;
        }
        Document document = getDocument(attachment, processAPI);
        if (document == null) {
            throw new ConnectorException("Document " + attachment + " does not exist");
        } else if (document.hasContent()) {
            addBodyPart(processAPI, bodyParts, document);
        } else if (document.getUrl() != null) {
            if (html) {
                messageBody.append("<br>");
            } else {
                messageBody.append("\n ");
            }
            messageBody.append(document.getName()).append(" : ").append(document.getUrl());
        }
    }

    private void addBodyPart(ProcessAPI processAPI, List<MimeBodyPart> bodyParts, Document document)
            throws DocumentNotFoundException, MessagingException {
        String fileName = document.getContentFileName();
        byte[] docContent = processAPI.getDocumentContent(document.getContentStorageId());
        if (docContent != null) {
            String mimeType = document.getContentMimeType();
            var bodyPart = new MimeBodyPart();
            final DataSource source = new ByteArrayDataSource(docContent, mimeType);
            final DataHandler dataHandler = new DataHandler(source);
            bodyPart.setDataHandler(dataHandler);
            bodyPart.setFileName(fileName);
            bodyParts.add(bodyPart);
        }
    }

    private Document getDocument(Object attachment, ProcessAPI processAPI)
            throws ConnectorException, DocumentNotFoundException {
        if (attachment instanceof String && !((String) attachment).trim().isEmpty()) {
            String docName = (String) attachment;
            long processInstanceId = getExecutionContext().getProcessInstanceId();
            return processAPI.getLastDocument(processInstanceId, docName);
        } else if (attachment instanceof Document) {
            return (Document) attachment;
        } else {
            throw new ConnectorException(
                    "Attachments must be document names or org.bonitasoft.engine.bpm.document.Document");
        }
    }

    @Override
    protected void executeBusinessLogic() throws ConnectorException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            final Session session = getSession();
            Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
            final Message email = getEmail(session);
            Transport.send(email);
        } catch (final Exception e) {
            throw new ConnectorException(e);
        } finally {
            Thread.currentThread().setContextClassLoader(classLoader);
        }
    }
}
