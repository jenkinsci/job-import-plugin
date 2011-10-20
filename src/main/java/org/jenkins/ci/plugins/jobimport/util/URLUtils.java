/*
 * The MIT License
 * 
 * Copyright (c) 2011, Jesse Farinacci, Manufacture Francaise des Pneumatiques Michelin,
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
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.jenkins.ci.plugins.jobimport.util;

import org.apache.commons.io.IOUtils;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.conn.ssl.SSLSocketFactory;
import static org.apache.commons.lang.Validate.notNull;

/**
 * @author <a href="mailto:jieryn@gmail.com">Jesse Farinacci</a>
 * @since 1.0
 */
public final class URLUtils {

  public static String fetchUrl(String url) throws IOException {
    notNull(url);

    if (url.toUpperCase().startsWith("HTTPS")) {
      enableSSLSupport();
    }

    return fetchUrl(new URL(url));
  }

  /**
   * Enables the SSL support (trusts all SSL certificates).
   */
  private static void enableSSLSupport() {
    TrustManager[] trustAllCerts = new TrustManager[]{
      new X509TrustManager() {
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
          return null;
        }

        public void checkClientTrusted(
          java.security.cert.X509Certificate[] certs, String authType) {
        }

        public void checkServerTrusted(
          java.security.cert.X509Certificate[] certs, String authType) {
        }
      }
    };

    SSLContext sc = null;
    try {
      sc = SSLContext.getInstance("TLS");
      sc.init(null, trustAllCerts, new java.security.SecureRandom());

      HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
      HttpsURLConnection.setDefaultHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
    } catch (Exception e) {
      LOG.log(Level.WARNING, "Unable to enable the SSL support", e);
    }
  }

  public static String fetchUrl(final URL url) throws IOException {
    notNull(url);

    InputStream inputStream = null;

    try {
      inputStream = url.openStream();
      return IOUtils.toString(inputStream);
    }

    finally {
      IOUtils.closeQuietly(inputStream);
    }
  }

  /**
   * Static-only access.
   */
  private URLUtils() {
    // static-only access
  }

  private static final Logger LOG = Logger.getLogger(URLUtils.class.getName());

}
