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
import java.io.InputStream;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author <a href="mailto:jieryn@gmail.com">Jesse Farinacci</a>
 * @since 1.0
 */
public final class XPathUtils {

  private static final DocumentBuilderFactory DOCUMENT_BUILDER_FACTORY = DocumentBuilderFactory.newInstance();

  private static final XPathFactory           XPATH_FACTORY            = XPathFactory.newInstance();

  public static XPathExpression compile(final String expression) throws XPathExpressionException {
    notNull(expression);

    return XPATH_FACTORY.newXPath().compile(expression);
  }

  public static List<String> evaluateNodeListTextXPath(final Document document, final String expression)
      throws XPathExpressionException {
    return evaluateNodeListTextXPath(document, compile(expression));
  }

  public static List<String> evaluateNodeListTextXPath(final Document document, final XPathExpression expression)
      throws XPathExpressionException {
    notNull(document);
    notNull(expression);

    final List<String> result = new LinkedList<String>();

    for (final Node node : evaluateNodeListXPath(document, expression)) {
      result.add(node.getTextContent());
    }

    return result;
  }

  public static List<Node> evaluateNodeListXPath(final Document document, final String expression)
      throws XPathExpressionException {
    return evaluateNodeListXPath(document, compile(expression));
  }

  public static List<Node> evaluateNodeListXPath(final Document document, final XPathExpression expression)
      throws XPathExpressionException {
    notNull(document);
    notNull(expression);

    final List<Node> result = new LinkedList<Node>();

    final NodeList nodeList = (NodeList) expression.evaluate(document, XPathConstants.NODESET);

    for (int node = 0; node < nodeList.getLength(); node++) {
      result.add(nodeList.item(node));
    }

    return result;
  }

  public static String evaluateStringXPath(final Document document, final String expression)
      throws XPathExpressionException {
    return evaluateStringXPath(document, compile(expression));
  }

  public static String evaluateStringXPath(final Document document, final XPathExpression expression)
      throws XPathExpressionException {
    notNull(document);
    notNull(expression);

    return (String) expression.evaluate(document, XPathConstants.STRING);
  }

  public static void notNull(final Object object) {
    if (object == null) {
      throw new IllegalArgumentException();
    }
  }

  public static Document parse(final InputStream inputStream) throws SAXException, IOException,
      ParserConfigurationException {
    notNull(inputStream);

    return DOCUMENT_BUILDER_FACTORY.newDocumentBuilder().parse(inputStream);
  }

  public static Document parse(final String input) throws SAXException, IOException, ParserConfigurationException {
    notNull(input);

    final InputStream inputStream = IOUtils.toInputStream(input);

    try {
      return XPathUtils.parse(inputStream);
    }

    finally {
      IOUtils.closeQuietly(inputStream);
    }
  }

  public static Document parse(final URL url) throws SAXException, IOException, ParserConfigurationException {
    notNull(url);

    final InputStream inputStream = url.openStream();

    try {
      return parse(inputStream);
    }

    finally {
      IOUtils.closeQuietly(inputStream);
    }
  }

  /**
   * Static-only access.
   */
  private XPathUtils() {
    // static-only class
  }
}
