/*
 * The MIT License
 *
 * Copyright (c) 2011, Manufacture Francaise des Pneumatiques Michelin,
 * Daniel Petisme, Romain Seguy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.jenkins.ci.plugins.jobimport.util;

import org.apache.commons.lang.StringUtils;
import org.apache.http.*;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.jenkins.ci.plugins.jobimport.model.JenkinsInstance;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Helper class aimed at invoking Jenkins' <a href="https://wiki.jenkins-ci.org/display/JENKINS/Remote+access+API">API</a>.
 * 
 * @author Daniel Petisme <daniel.petisme@gmail.com> <http://danielpetisme.blogspot.com/>
 */
public class JenkinsAPIUtils {

    private JenkinsAPIUtils() {
    }
    
    /**
     * Retrieves an available resource on a Jenkins instance using Jenkins'
     * REST-like API.
     *      
     * @param jenkinsInstance represents the Jenkins instance to request
     * @param resourceUrl specifies where to find the resource
     * @return the resource (in {@code PLAIN TEXT} format)
     */
    public static String getResource(final JenkinsInstance jenkinsInstance, final String resourceUrl) throws IOException, HttpException {
        Executor executor = new Executor();
        String resource = null;

        try {
            HttpGet getConfig = new HttpGet(resourceUrl);
            HttpResponse response = executor.execute(jenkinsInstance, getConfig);

            if (response != null) {
                if (response.getStatusLine() != null) {
                    if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                        resource = EntityUtils.toString(response.getEntity());
                    } else if (response.getStatusLine().getStatusCode() == HttpStatus.SC_FORBIDDEN) {
                        LOGGER.log(Level.WARNING, "Authentication failed on {0}", jenkinsInstance.getUrl());
                        throw new HttpException(Messages._JenkinsAPIUtils_authentication_failed(jenkinsInstance.getUrl()).toString());
                    } else {
                        LOGGER.log(Level.WARNING, "HTTP response: {0}", response.getStatusLine().getStatusCode());
                        throw new HttpException(Messages._JenkinsAPIUtils_failed(resourceUrl, response.getStatusLine().getStatusCode()).toString());
                    }
                }
            }

            executor.close();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Unable to get the resource " + resource, e);
            executor.close();
            throw e;
        }

        return resource;
    }
    
    private static class Executor {
        
        private DefaultHttpClient client;

        private Executor() {
            client = new DefaultHttpClient();

            KeyStore trustStore = null;
            try {
                trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
                trustStore.load(null, null);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "HTTPS initialization issue", e);
            }

            try {
                client.getConnectionManager().getSchemeRegistry().register(new Scheme("https", 443, SimpleSSLSocketFactory.createSimpleSSLSocketFactory()));
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "SSL support failed", e);
            }

            client.setRedirectStrategy(new DefaultRedirectStrategy() {
                @Override
                public boolean isRedirected(HttpRequest request, HttpResponse response, HttpContext context) {
                    return response.getStatusLine().getStatusCode() == HttpStatus.SC_MOVED_TEMPORARILY;
                }

                @Override
                public HttpUriRequest getRedirect(HttpRequest request, HttpResponse response, HttpContext context) {
                    LOGGER.log(Level.FINE, "Redirect HTTP response: {0}", response.getStatusLine().getStatusCode());
                    return new HttpGet(response.getFirstHeader("Location").getValue());
                }
            });
        }

        /**
         * Authenticates the user.
         *      
         * @param authType the authentication type ({@code j_security_check} or
         *                 {@code j_acegi_security_check})
         * @param jenkinsInstance the Jenkins instance we want to log in
         * @return the HTTP response gotten from the Jenkins instance
         */
        private HttpResponse login(final String authType, final JenkinsInstance jenkinsInstance) throws IOException {
            HttpPost postLogin = new HttpPost(jenkinsInstance.getUrl() + authType);

            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            nvps.add(new BasicNameValuePair("j_username", jenkinsInstance.getLogin()));
            nvps.add(new BasicNameValuePair("j_password", jenkinsInstance.getPassword()));

            postLogin.getParams().setParameter("Login", "login");

            try {
                postLogin.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
            } catch (UnsupportedEncodingException e) {
                LOGGER.log(Level.WARNING, "Encoding issue", e);
            }

            HttpResponse response = client.execute(postLogin);
            ignoreResponse(response);

            return response;
        }

        /**
         * Executes an HTTP method (and, if required, manages the HTTP authentication).
         *      
         * @param jenkinsInstance the Jenkins instance from which to get credentials (if needed)
         * @param method the request to be executed on the remote Jenkins instance
         * @return the HTTP response gotten from the Jenkins instance
         */
        private HttpResponse execute(final JenkinsInstance jenkinsInstance, HttpRequestBase method) throws IOException {
            if (StringUtils.isNotBlank(jenkinsInstance.getLogin())) {
                HttpGet loginMethod = new HttpGet(jenkinsInstance.getUrl() + "loginEntry");
                ignoreResponse(client.execute(loginMethod));

                LOGGER.log(Level.FINE, "Trying to use j_security_check authentication mode");
                HttpResponse response = login("j_security_check", jenkinsInstance);

                ignoreResponse(response);

                if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                    LOGGER.log(Level.FINE, "Trying to use j_acegi_security_check authentication mode");
                    ignoreResponse(login("j_acegi_security_check", jenkinsInstance));
                }
            }

            return client.execute(method);
        }

        private void close() {
            client.getConnectionManager().shutdown();
        }

        /**
         * Consumes an HTTP response ({@link HttpEntity}).
         *      
         * @param response the HTTP response to consume
         */
        private void ignoreResponse(HttpResponse response) throws IOException {
            if (response != null) {
                if (response.getEntity() != null) {
                    EntityUtils.consume(response.getEntity());
                }
            }
        }

    }

    /**
     * Provides SSL support.
     */
    private static class SimpleSSLSocketFactory extends SSLSocketFactory {

        private static SSLContext sslContext;

        /**
         * Create The appropriate {@link SSLSocketFactory} wich will accept all
         * the certificates.
         *
         * @return an HTTPS-compatible {@link SSLSocketFactory}
         */
        public static SimpleSSLSocketFactory createSimpleSSLSocketFactory() throws KeyManagementException, NoSuchAlgorithmException {
            sslContext = SSLContext.getInstance("TLS");

            TrustManager tm = new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                }

                public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };

            sslContext.init(null, new TrustManager[]{tm}, null);

            return new SimpleSSLSocketFactory(sslContext);
        }

        public SimpleSSLSocketFactory(SSLContext sslContext) {
            super(sslContext, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        }

        @Override
        public Socket createLayeredSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
            return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
        }

    }

    private static final transient Logger LOGGER = Logger.getLogger(JenkinsAPIUtils.class.getName());

}
