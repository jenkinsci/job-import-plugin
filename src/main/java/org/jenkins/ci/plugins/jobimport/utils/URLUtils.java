/*
 * The MIT License
 *
 * Copyright (c) 2011, Jesse Farinacci
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

package org.jenkins.ci.plugins.jobimport.utils;

import org.acegisecurity.AccessDeniedException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * @author <a href="mailto:jieryn@gmail.com">Jesse Farinacci</a>
 * @since 1.0
 */
public final class URLUtils {

  public static void notNull(final Object object) {
    if (object == null) {
      throw new IllegalArgumentException();
    }
  }

    /**
     *
     * @param url The url to fetch
     * @param username The username to use while fetching the url
     * @param password The password to use while fetching the url
     * @return The HttpResponse received.
     * @throws IOException If there was an issue in the communication with the server
     */
    public static HttpResponse getUrl(String url, String username, String password) throws IOException {
        notNull(url);
        notNull(username);
        notNull(password);
        HttpClientBuilder builder = HttpClients.custom();
        HttpClientContext localContext = HttpClientContext.create();

        URL _url = new URL(url);
        String userInfo = _url.getUserInfo();
        HttpHost target = new HttpHost(_url.getHost(), _url.getPort(), _url.getProtocol());

        UsernamePasswordCredentials usernamePasswordCredentials = null;
        if (userInfo != null && !userInfo.isEmpty()) {
            usernamePasswordCredentials = new UsernamePasswordCredentials(userInfo);
        }
        if(!username.isEmpty()) {
            usernamePasswordCredentials = new UsernamePasswordCredentials(username, password);
        }

        if(usernamePasswordCredentials != null) {
            CredentialsProvider credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(//AuthScope.ANY,
                    new AuthScope(_url.getHost(), _url.getPort()),
                    usernamePasswordCredentials);

            builder.setDefaultCredentialsProvider(credsProvider);

            AuthCache authCache = new BasicAuthCache();
            // Generate BASIC scheme object and add it to the local
            // auth cache
            BasicScheme basicAuth = new BasicScheme();
            authCache.put(target, basicAuth);

            localContext.setAuthCache(authCache);

        }
        return builder.build().execute(target, new HttpGet(url), localContext);
    }
    
    public static InputStream fetchUrl(String url, String username, String password) throws IOException {
        HttpResponse response = getUrl(url, username, password);
        return response.getEntity().getContent();
    }


    public static String safeURL(String base, String sufix) {
        if (base.endsWith("/") && sufix.startsWith("/")) {
            return base.substring(0,base.length() - 1) + sufix;
        } else if (base.endsWith("/") && !sufix.startsWith("/")) {
            return base + sufix;
        } else if (!base.endsWith("/") && sufix.startsWith("/")) {
            return base + sufix;
        } else {
            return base + "/" + sufix;
        }
    }
  /**
   * Static-only access.
   */
  private URLUtils() {
    // static-only access
  }
}
