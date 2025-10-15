# Claude Code Instructions

This document provides guidance for AI assistants working with the Bonita Email Connector codebase.

## Project Overview

The Bonita Email Connector is a Java-based connector for the Bonita BPM platform that enables sending emails from business processes. It supports HTML/plain text emails, attachments, multiple recipients, and various SMTP configurations including SSL/TLS encryption.

## Key Technologies

- **Java 11+**: Primary programming language
- **Maven**: Build system and dependency management
- **Jakarta Mail API**: Email composition and SMTP communication
- **Bonita Connector Framework**: Integration with Bonita BPM
- **JUnit 5**: Testing framework
- **GreenMail**: Mock SMTP server for testing
- **Testcontainers**: Integration testing with real Bonita instances

## Project Structure

```
bonita-connector-email/
├── src/main/java/org/bonitasoft/connectors/email/
│   ├── EmailConnector.java           # Main connector implementation
│   ├── SMTPAuthenticator.java        # SMTP authentication handler
│   └── XOAUTH2Authenticator.java     # OAuth2 authentication (if present)
├── src/main/resources-filtered/
│   └── email.def                      # Connector definition (XML)
├── src/test/java/                     # Unit and integration tests
├── pom.xml                            # Maven project configuration
└── README.md                          # Project documentation
```

## Important Files

### EmailConnector.java (src/main/java/org/bonitasoft/connectors/email/EmailConnector.java)

Main connector implementation with key methods:
- `validateInputParameters()`: Validates configuration parameters (line ~157)
- `executeBusinessLogic()`: Main execution logic that sends emails (line ~514)
- `getSession()`: Configures SMTP session with authentication (line ~271)
- `getEmail()`: Constructs MIME message with headers (line ~345)
- `getMultipart()`: Handles attachments from Bonita documents (line ~412)

### email.def (src/main/resources-filtered/email.def)

XML connector definition that defines:
- Input parameters and their types
- Output parameters
- Connector metadata and categories
- UI configuration for Bonita Studio

## Building and Testing

### Build Commands
```bash
# Clean and build
./mvnw clean package

# Run tests
./mvnw test

# Run integration tests
./mvnw verify

# Skip tests
./mvnw package -DskipTests
```

### Test Structure
- Unit tests use GreenMail mock SMTP server
- Integration tests use Testcontainers with real Bonita runtime
- Tests cover SSL/TLS, authentication, attachments, and error cases

## Common Development Tasks

### Adding New Parameters

1. Add input constant in `EmailConnector.java` (with other INPUT_* constants)
2. Update `validateInputParameters()` if validation needed
3. Use the parameter in `executeBusinessLogic()` or helper methods
4. Add input definition to `email.def`
5. Write tests for the new parameter

### Modifying Email Logic

- Email composition: `getEmail()` method
- Attachment handling: `getMultipart()` method
- SMTP session: `getSession()` method
- Authentication: `SMTPAuthenticator.java`

### Security Considerations

- Never log passwords or sensitive data
- Validate all email addresses to prevent injection
- Use SSL/TLS in production (trustCertificate should be false)
- Sanitize user input in email content

## Coding Conventions

- Follow existing code style and formatting
- Use meaningful variable names
- Add javadoc comments for public methods
- Keep methods focused and reasonably sized
- Handle exceptions appropriately with clear error messages
- Write tests for new functionality

## Git Workflow

- Main branch: `master`
- Create feature branches for new work
- Write clear commit messages
- Reference issues in commits when applicable
- Ensure tests pass before committing

## Dependencies

Key dependencies (managed in `pom.xml`):
- `org.bonitasoft.engine:bonita-common` - Bonita API
- `jakarta.mail:jakarta.mail-api` - Email API
- `org.eclipse.angus:angus-mail` - Mail implementation
- `com.icegreen:greenmail-junit5` - Testing
- `org.testcontainers` - Integration testing

## Connector Parameters Reference

### Required
- `smtpHost`: SMTP server hostname
- `smtpPort`: SMTP port (1-65535)
- `from`: Sender email address
- `subject`: Email subject
- `message`: Email body
- At least one recipient: `to`, `cc`, or `bcc`

### Optional
- `userName`, `password`: SMTP authentication
- `sslSupport`, `starttlsSupport`: Encryption
- `html`: HTML vs plain text (default: true)
- `charset`: Character encoding (default: UTF-8)
- `attachments`: List of document names or Document objects
- `headers`: Custom email headers as list of [key, value] pairs
- `replyTo`, `returnPath`: Email routing headers

## Testing Guidelines

- Use GreenMail for SMTP server mocking
- Test both success and failure scenarios
- Cover SSL/TLS configurations
- Test attachment handling (documents with/without content)
- Verify error messages are clear and helpful
- Test email address validation
- Check authentication mechanisms

## Release Process

1. Update version in `pom.xml`
2. Run full test suite: `./mvnw verify`
3. Build connector package: `./mvnw clean package`
4. Connector ZIP is generated in `target/` directory
5. Tag release in git
6. Publish to Maven Central (if applicable)

## Additional Resources

- [Bonita Documentation](https://documentation.bonitasoft.com)
- [Bonita Connector Development Guide](https://documentation.bonitasoft.com/bonita/latest/software-extensibility/connectors)
- [Jakarta Mail API Documentation](https://eclipse-ee4j.github.io/mail/)
- [Project README](README.md)
- [Contributing Guidelines](CONTRIBUTING.md)

## When Working on This Project

1. **Read relevant code first**: Use Read tool to examine files before making changes
2. **Understand the context**: This is a connector that runs within Bonita, not a standalone app
3. **Test thoroughly**: Email functionality is critical for business processes
4. **Consider backwards compatibility**: Existing processes depend on this connector
5. **Document changes**: Update this file and README.md as needed
6. **Follow security best practices**: Email connectors handle sensitive data
