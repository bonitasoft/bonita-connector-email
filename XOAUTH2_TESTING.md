# XOAUTH2 Authentication Testing Guide

This guide explains how to test the XOAUTH2 authentication feature of the Bonita Email Connector.

## Overview

The connector now supports OAuth2 authentication using the SASL XOAUTH2 mechanism, which is required by modern email providers like Office 365 and Gmail.

## Automated Tests

Run automated configuration tests to verify XOAUTH2 properties are set correctly:

```bash
./mvnw test -Dtest=EmailConnectorXOAuth2IT
```

These tests verify:
- XOAUTH2 session properties are configured correctly
- Office 365 SMTP configuration
- Gmail SMTP configuration
- Non-XOAUTH2 mode still works correctly

**Note**: GreenMail (the mock SMTP server used for testing) does not support XOAUTH2 authentication. Automated tests verify configuration only, not actual email sending with OAuth2.

## Manual Integration Testing

To test actual email sending with XOAUTH2, you need to run manual tests against real email providers.

### Prerequisites

You need valid OAuth2 credentials from one of the supported providers:

- **Office 365**: Azure AD application with `SMTP.Send` permission
- **Gmail**: Google Cloud project with Gmail API enabled

### Testing with Office 365

#### Step 1: Obtain an Access Token

Use Azure CLI to get an access token:

```bash
# Login to Azure
az login

# Get access token for Office 365 SMTP
az account get-access-token --resource https://outlook.office.com
```

Or use your Azure AD application's OAuth2 flow to obtain a token with these scopes:
- `https://outlook.office.com/SMTP.Send`
- `https://outlook.office.com/.default`

#### Step 2: Set Environment Variables

```bash
export SMTP_OAUTH2_USER="your-email@company.com"
export SMTP_OAUTH2_TOKEN="your-access-token-here"
export SMTP_OAUTH2_TO="recipient@example.com"
```

#### Step 3: Run the Test

Enable and run the manual Office 365 test:

```java
// In EmailConnectorXOAuth2IT.java, remove @Disabled annotation from:
@Test
void testManualXOAuth2WithOffice365() throws Exception {
    // ...
}
```

Then run:

```bash
./mvnw test -Dtest=EmailConnectorXOAuth2IT#testManualXOAuth2WithOffice365
```

#### Expected Result

If successful, you should see:
```
Email sent successfully using XOAUTH2 authentication!
```

The recipient should receive an email with subject "XOAUTH2 Test from Bonita Connector".

### Testing with Gmail

#### Step 1: Set Up OAuth2 Credentials

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create or select a project
3. Enable Gmail API
4. Create OAuth2 credentials (Desktop app type)
5. Use OAuth2 flow to obtain an access token with scope: `https://mail.google.com/`

#### Step 2: Set Environment Variables

```bash
export GMAIL_OAUTH2_USER="your-email@gmail.com"
export GMAIL_OAUTH2_TOKEN="your-gmail-access-token"
export GMAIL_OAUTH2_TO="recipient@example.com"
```

#### Step 3: Run the Test

Enable and run the manual Gmail test:

```java
// In EmailConnectorXOAuth2IT.java, remove @Disabled annotation from:
@Test
void testManualXOAuth2WithGmail() throws Exception {
    // ...
}
```

Then run:

```bash
./mvnw test -Dtest=EmailConnectorXOAuth2IT#testManualXOAuth2WithGmail
```

## SMTP Configuration Reference

### Office 365 / Exchange Online

```properties
smtpHost=smtp.office365.com
smtpPort=587
sslSupport=false
starttlsSupport=true
authType=Oauth (XOAUTH2)
userName=user@company.com
oauth2AccessToken=<your-access-token>
```

### Gmail

```properties
smtpHost=smtp.gmail.com
smtpPort=587
sslSupport=false
starttlsSupport=true
authType=Oauth (XOAUTH2)
userName=user@gmail.com
oauth2AccessToken=<your-access-token>
```

## Implementation Details

When XOAUTH2 is enabled (by setting `authType=Oauth (XOAUTH2)`), the connector sets these JavaMail properties:

```properties
mail.smtp.auth=true
mail.smtp.auth.mechanisms=XOAUTH2
mail.smtp.sasl.enable=true
mail.smtp.sasl.mechanisms=XOAUTH2
```

The `XOAUTH2Authenticator` class handles the authentication by:
1. Accepting the username and OAuth2 access token
2. Returning them via `getPasswordAuthentication()`
3. JavaMail's built-in XOAUTH2 SASL mechanism automatically encodes the credentials and handles the protocol details

## Troubleshooting

### Token Expiration

OAuth2 access tokens typically expire after 60-90 minutes. If you get authentication failures:

1. Check if your token has expired
2. Obtain a new access token
3. Retry the test

### Authentication Errors

Common issues:

- **Invalid token**: Ensure your token has the correct scopes (`SMTP.Send` for Office 365, `https://mail.google.com/` for Gmail)
- **Expired token**: Obtain a fresh token
- **Wrong user**: Ensure `userName` matches the email address associated with the token
- **Missing SASL support**: Ensure `mail.smtp.sasl.enable=true` is set (automatically configured by the connector)

### Testing Locally Without OAuth2

For local development without OAuth2:

```java
// Use traditional basic authentication
parameters.put(EmailConnector.AUTH_TYPE, EmailConnector.BASIC_AUTH_TYPE);
parameters.put(EmailConnector.USER_NAME, "testuser");
parameters.put(EmailConnector.PASSWORD, "testpassword");
```

## OAuth2 Flow Integration

In production, you should:

1. **Store refresh tokens securely** in Bonita's credential storage
2. **Implement token refresh logic** in your process to obtain new access tokens before they expire
3. **Handle authentication errors** gracefully with retry logic
4. **Use proper scopes**:
   - Office 365: `https://outlook.office.com/SMTP.Send`
   - Gmail: `https://mail.google.com/`

### Token Refresh Example

Your Bonita process should include a service task that:

1. Retrieves the refresh token from secure storage
2. Calls the OAuth2 token endpoint to get a new access token
3. Stores the new access token (and optionally updated refresh token)
4. Passes the access token to the Email Connector

Example for Office 365 using Azure AD:

```bash
curl -X POST https://login.microsoftonline.com/{tenant}/oauth2/v2.0/token \
  -d "client_id={client_id}" \
  -d "client_secret={client_secret}" \
  -d "refresh_token={refresh_token}" \
  -d "grant_type=refresh_token" \
  -d "scope=https://outlook.office.com/SMTP.Send"
```

## Security Best Practices

1. **Never log access tokens** - The connector already excludes passwords from logs
2. **Store tokens securely** - Use Bonita's credential storage or a secrets manager
3. **Use least-privilege scopes** - Only request SMTP.Send, not full mailbox access
4. **Rotate credentials regularly** - Implement token rotation policies
5. **Monitor for failures** - Set up alerts for authentication failures

## References

- [Microsoft OAuth2 SMTP Documentation](https://learn.microsoft.com/en-us/exchange/client-developer/legacy-protocols/how-to-authenticate-an-imap-pop-smtp-application-by-using-oauth)
- [Google OAuth2 SMTP Documentation](https://developers.google.com/gmail/imap/xoauth2-protocol)
- [JavaMail OAuth2 Support](https://javaee.github.io/javamail/OAuth2)
- [Bonita Connector Development](https://documentation.bonitasoft.com/bonita/latest/software-extensibility/connectors)
