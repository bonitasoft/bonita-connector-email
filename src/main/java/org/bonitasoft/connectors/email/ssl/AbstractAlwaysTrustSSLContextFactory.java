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
package org.bonitasoft.connectors.email.ssl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public abstract class AbstractAlwaysTrustSSLContextFactory extends SSLSocketFactory {

    private SSLSocketFactory factory;

    public static class AlwaysTrustManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] arg0, String arg1)
                throws CertificateException {
            // No check
        }

        @Override
        public void checkServerTrusted(X509Certificate[] arg0, String arg1)
                throws CertificateException {
            // No check
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

    protected AbstractAlwaysTrustSSLContextFactory() throws NoSuchAlgorithmException, KeyManagementException {
        super();
        SSLContext ctx = getSSLContext();
        ctx.init(new KeyManager[0], new  TrustManager[]{ new AlwaysTrustManager() }, new SecureRandom());
        factory = ctx.getSocketFactory();
    }

    protected abstract SSLContext getSSLContext() throws NoSuchAlgorithmException, KeyManagementException;

    @Override
    public Socket createSocket() throws IOException {
        return factory.createSocket();
    }

    public Socket createSocket(InetAddress address, int port,
            InetAddress localAddress, int localPort)
            throws IOException {
        return factory.createSocket(address, port, localAddress, localPort);
    }

    public Socket createSocket(InetAddress host, int port) throws IOException {
        return factory.createSocket(host, port);
    }

    public Socket createSocket(Socket s, String host, int port,
            boolean autoClose) throws IOException {
        return factory.createSocket(s, host, port, autoClose);
    }

    public Socket createSocket(String host, int port, InetAddress localHost,
            int localPort) throws IOException {
        return factory.createSocket(host, port, localHost, localPort);
    }

    public Socket createSocket(String host, int port) throws IOException {
        return factory.createSocket(host, port);
    }

    @Override
    public boolean equals(Object obj) {
        return factory.equals(obj);
    }
    
    @Override
    public int hashCode() {
        return factory.hashCode();
    }

    public String[] getDefaultCipherSuites() {
        return factory.getDefaultCipherSuites();
    }

    public String[] getSupportedCipherSuites() {
        return factory.getSupportedCipherSuites();
    }

}
