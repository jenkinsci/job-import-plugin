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

package org.jenkins.ci.plugins.jobimport;

import java.io.IOException;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * @author <a href="mailto:jieryn@gmail.com">Jesse Farinacci</a>
 * @since 1.0
 */
public final class RemoteJobUtils {

  private static final Logger          LOG = Logger.getLogger(RemoteJobUtils.class.getName());

  private static final XPathExpression XPATH_EXPRESSION_FREE_STYLE_PROJECT_DESCRIPTION;

  private static final XPathExpression XPATH_EXPRESSION_FREE_STYLE_PROJECT_NAME;

  private static final XPathExpression XPATH_EXPRESSION_FREE_STYLE_PROJECT_URL;

  private static final XPathExpression XPATH_EXPRESSION_HUDSON_JOB_URL;

  private static final XPathExpression XPATH_EXPRESSION_LIST_VIEW_JOB_URL;

  private static final XPathExpression XPATH_EXPRESSION_MAVEN_MODULE_SET_DESCRIPTION;

  private static final XPathExpression XPATH_EXPRESSION_MAVEN_MODULE_SET_NAME;

  private static final XPathExpression XPATH_EXPRESSION_MAVEN_MODULE_SET_URL;

  private static final XPathExpression XPATH_EXPRESSION_MATRIX_PROJECT_NAME;

  private static final XPathExpression XPATH_EXPRESSION_MATRIX_PROJECT_URL;

  private static final XPathExpression XPATH_EXPRESSION_MATRIX_PROJECT_DESCRIPTION;

  static {
    try {
      XPATH_EXPRESSION_FREE_STYLE_PROJECT_DESCRIPTION = XPathUtils.compile("/freeStyleProject/description");
      XPATH_EXPRESSION_FREE_STYLE_PROJECT_NAME = XPathUtils.compile("/freeStyleProject/name");
      XPATH_EXPRESSION_FREE_STYLE_PROJECT_URL = XPathUtils.compile("/freeStyleProject/url");
      XPATH_EXPRESSION_HUDSON_JOB_URL = XPathUtils.compile("/hudson/job/url");
      XPATH_EXPRESSION_LIST_VIEW_JOB_URL = XPathUtils.compile("/listView/job/url");
      XPATH_EXPRESSION_MAVEN_MODULE_SET_DESCRIPTION = XPathUtils.compile("/mavenModuleSet/description");
      XPATH_EXPRESSION_MAVEN_MODULE_SET_NAME = XPathUtils.compile("/mavenModuleSet/name");
      XPATH_EXPRESSION_MAVEN_MODULE_SET_URL = XPathUtils.compile("/mavenModuleSet/url");
      XPATH_EXPRESSION_MATRIX_PROJECT_NAME = XPathUtils.compile("/matrixProject/name");
      XPATH_EXPRESSION_MATRIX_PROJECT_URL = XPathUtils.compile("/matrixProject/url");
      XPATH_EXPRESSION_MATRIX_PROJECT_DESCRIPTION =  XPathUtils.compile("/matrixProject/description");;
    }

    catch (final XPathExpressionException e) {
      LOG.log(Level.WARNING, e.getMessage(), e);
      throw new IllegalStateException(e);
    }
  }

  protected static RemoteJob fromFreeStyleProjectXml(final String xml) throws SAXException, IOException,
      ParserConfigurationException, XPathExpressionException {
    if (StringUtils.isEmpty(xml)) {
      return null;
    }

    if (!xml.startsWith("<freeStyleProject>")) {
      return null;
    }

    final Document document = XPathUtils.parse(xml);

    return new RemoteJob(XPathUtils.evaluateStringXPath(document, XPATH_EXPRESSION_FREE_STYLE_PROJECT_NAME),
        XPathUtils.evaluateStringXPath(document, XPATH_EXPRESSION_FREE_STYLE_PROJECT_URL),
        XPathUtils.evaluateStringXPath(document, XPATH_EXPRESSION_FREE_STYLE_PROJECT_DESCRIPTION));
  }

  protected static SortedSet<RemoteJob> fromHudsonXml(final String xml) throws SAXException, IOException,
      ParserConfigurationException, XPathExpressionException {
    final SortedSet<RemoteJob> remoteJobs = new TreeSet<RemoteJob>();

    if (StringUtils.isEmpty(xml)) {
      return remoteJobs;
    }

    if (!xml.startsWith("<hudson>")) {
      return remoteJobs;
    }

    final Document document = XPathUtils.parse(xml);

    for (final String jobUrl : XPathUtils.evaluateNodeListTextXPath(document, XPATH_EXPRESSION_HUDSON_JOB_URL)) {
      remoteJobs.addAll(fromXml(URLUtils.fetchUrl(jobUrl + "/api/xml")));
    }

    return remoteJobs;
  }

  protected static SortedSet<RemoteJob> fromListViewXml(final String xml) throws SAXException, IOException,
      ParserConfigurationException, XPathExpressionException {
    final SortedSet<RemoteJob> remoteJobs = new TreeSet<RemoteJob>();

    if (StringUtils.isEmpty(xml)) {
      return remoteJobs;
    }

    if (!xml.startsWith("<listView>")) {
      return remoteJobs;
    }

    final Document document = XPathUtils.parse(xml);

    for (final String jobUrl : XPathUtils.evaluateNodeListTextXPath(document, XPATH_EXPRESSION_LIST_VIEW_JOB_URL)) {
      remoteJobs.addAll(fromXml(URLUtils.fetchUrl(jobUrl + "/api/xml")));
    }

    return remoteJobs;
  }

  protected static RemoteJob fromMavenModuleSetXml(final String xml) throws SAXException, IOException,
      ParserConfigurationException, XPathExpressionException {
    if (StringUtils.isEmpty(xml)) {
      return null;
    }

    if (!xml.startsWith("<mavenModuleSet>")) {
      return null;
    }

    final Document document = XPathUtils.parse(xml);

    return new RemoteJob(XPathUtils.evaluateStringXPath(document, XPATH_EXPRESSION_MAVEN_MODULE_SET_NAME),
        XPathUtils.evaluateStringXPath(document, XPATH_EXPRESSION_MAVEN_MODULE_SET_URL),
        XPathUtils.evaluateStringXPath(document, XPATH_EXPRESSION_MAVEN_MODULE_SET_DESCRIPTION));
  }

  protected static RemoteJob fromMatrixProjectXml(final String xml) throws SAXException, IOException,
  ParserConfigurationException, XPathExpressionException {
    if (StringUtils.isEmpty(xml)) {
      return null;
    }
    
    if (!xml.startsWith("<matrixProject>")) {
      return null;
    }
    
    final Document document = XPathUtils.parse(xml);
    
    return new RemoteJob(XPathUtils.evaluateStringXPath(document, XPATH_EXPRESSION_MATRIX_PROJECT_NAME),
        XPathUtils.evaluateStringXPath(document, XPATH_EXPRESSION_MATRIX_PROJECT_URL),
        XPathUtils.evaluateStringXPath(document, XPATH_EXPRESSION_MATRIX_PROJECT_DESCRIPTION));
  }

  public static SortedSet<RemoteJob> fromXml(final String xml) throws XPathExpressionException, SAXException,
      IOException, ParserConfigurationException {
    final SortedSet<RemoteJob> remoteJobs = new TreeSet<RemoteJob>();

    if (StringUtils.isEmpty(xml)) {
      return remoteJobs;
    }

    // ---

    if (xml.startsWith("<hudson>")) {
      remoteJobs.addAll(fromHudsonXml(xml));
    }

    else if (xml.startsWith("<freeStyleProject>")) {
      remoteJobs.add(fromFreeStyleProjectXml(xml));
    }

    else if (xml.startsWith("<listView>")) {
      remoteJobs.addAll(fromListViewXml(xml));
    }

    else if (xml.startsWith("<mavenModuleSet>")) {
      remoteJobs.add(fromMavenModuleSetXml(xml));
    }

    else if (xml.startsWith("<matrixProject>")) {
      remoteJobs.add(fromMatrixProjectXml(xml));
    }

    return remoteJobs;
  }

  /**
   * Static-only access.
   */
  private RemoteJobUtils() {
    // static-only access
  }
}
