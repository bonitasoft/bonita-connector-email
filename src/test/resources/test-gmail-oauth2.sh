#!/bin/bash
# Simple test to debug Gmail XOAUTH2 authentication

source env.txt

cat > /tmp/TestGmailOAuth2.java << 'EOF'
import javax.mail.*;
import java.util.Properties;

public class TestGmailOAuth2 {
    public static void main(String[] args) {
        String username = System.getenv("GMAIL_OAUTH2_USER");
        String accessToken = System.getenv("GMAIL_OAUTH2_TOKEN");

        System.out.println("Testing Gmail XOAUTH2 authentication...");
        System.out.println("User: " + username);
        System.out.println("Token length: " + (accessToken != null ? accessToken.length() : 0));

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.sasl.enable", "true");
        props.put("mail.smtp.sasl.mechanisms", "XOAUTH2");
        props.put("mail.smtp.auth.mechanisms", "XOAUTH2");
        props.put("mail.debug", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                System.out.println("Authenticator called!");
                return new PasswordAuthentication(username, accessToken);
            }
        });

        try {
            Transport transport = session.getTransport("smtp");
            System.out.println("Connecting to Gmail SMTP...");
            transport.connect();
            System.out.println("SUCCESS! Connected with XOAUTH2");
            transport.close();
        } catch (Exception e) {
            System.err.println("FAILED: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
EOF

# Compile and run
javac -cp "/opt/apache/repository/com/sun/mail/jakarta.mail/1.6.7/jakarta.mail-1.6.7.jar" /tmp/TestGmailOAuth2.java
java -cp "/tmp:/opt/apache/repository/com/sun/mail/jakarta.mail/1.6.7/jakarta.mail-1.6.7.jar:/opt/apache/repository/com/sun/activation/jakarta.activation/1.2.1/jakarta.activation-1.2.1.jar" TestGmailOAuth2
