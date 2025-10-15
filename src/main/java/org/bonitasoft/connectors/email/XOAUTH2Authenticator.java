/*
 * Copyright (C) 2009 - 2025 Bonitasoft S.A.
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

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

/**
 * The class XOAUTH2Authenticator represents an authenticator that supports
 * OAuth2 authentication (XOAUTH2 SASL) for SMTP connections.
 *
 * This authenticator works with JavaMail's built-in XOAUTH2 SASL mechanism.
 * When mail.smtp.sasl.mechanisms is set to "XOAUTH2", JavaMail automatically
 * handles the encoding of the authentication string in the correct format.
 *
 * For more information on XOAUTH2 SASL mechanism, see:
 * https://learn.microsoft.com/en-us/exchange/client-developer/legacy-protocols/how-to-authenticate-an-imap-pop-smtp-application-by-using-oauth
 *
 * @author Bonitasoft
 */
public class XOAUTH2Authenticator extends Authenticator {
    /**
     * The user name (email address) used for authentication.
     */
    private final String userName;

    /**
     * The OAuth2 access token used for authentication.
     */
    private final String accessToken;

    /**
     * Create an XOAUTH2Authenticator.
     *
     * @param username the user name (email address) used for authentication.
     * @param accessToken the OAuth2 access token used for authentication.
     */
    public XOAUTH2Authenticator(final String username, final String accessToken) {
        this.userName = username;
        this.accessToken = accessToken;
    }

    /**
     * Called when password authorization is needed.
     *
     * Returns the username and OAuth2 access token. JavaMail's XOAUTH2 SASL mechanism
     * will automatically encode these credentials in the correct format:
     * base64("user=username\u0001auth=Bearer token\u0001\u0001")
     */
    @Override
    public PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(userName, accessToken);
    }
}
