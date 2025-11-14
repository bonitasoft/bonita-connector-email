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
 * The class SMTPAuthenticator represents an object that knows how
 * to obtain authentication for a network connection with an SMTP server.
 *
 * @author chaffotm
 */
public class SMTPAuthenticator extends Authenticator {
    /**
     * The user name used for authentication.
     */
    private final String userName;

    /**
     * The password used for authentication.
     */
    private final String pwd;

    /**
     * Create an SMTPAuthenticator.
     * @param username the user name used for authentication.
     * @param password the password used for authentication.
     */
    public SMTPAuthenticator(final String username, final String password) {
        userName = username;
        pwd = password;
    }

    /**
     * Called when password authorization is needed.
     */
    @Override
    public PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(userName, pwd);
    }
}
