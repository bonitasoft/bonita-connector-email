package com.bonitasoft.presales.connectors.email.templating;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

public class SMTPAuthenticator extends Authenticator {
  /** The user name used for authentication. */
  private final String userName;

  /** The password used for authentication. */
  private final String pwd;

  /**
   * Create an SMTPAuthenticator.
   *
   * @param username the user name used for authentication.
   * @param password the password used for authentication.
   */
  public SMTPAuthenticator(final String username, final String password) {
    userName = username;
    pwd = password;
  }

  /** Called when password authorization is needed. */
  @Override
  public PasswordAuthentication getPasswordAuthentication() {
    return new PasswordAuthentication(userName, pwd);
  }
}
