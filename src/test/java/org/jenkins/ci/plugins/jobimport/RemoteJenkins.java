package org.jenkins.ci.plugins.jobimport;

import org.apache.commons.io.IOUtils;
import org.jenkins.ci.plugins.jobimport.utils.Constants;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.io.File.separator;

/**
 * Created by evildethow on 30/06/2016.
 */
final class RemoteJenkins {

  private static final String BASE_URL = "http://localhost";

  private static final String TOP_LVL_RSP_XML = "xml-api-response.xml";
  private static final String SECOND_LVL_RSP_XML = "folder" + separator + "xml-api-response.xml";
  private static final String THIRD_LVL_RSP_XML = "folder" + separator + "jobs" + separator + "aFolder" + separator + "xml-api-response.xml";

  private static final String TOP_LVL_QUERY = Constants.XML_API_QUERY;
  private static final String SECOND_LVL_FOLDER_QUERY = "/job/folder/" + Constants.XML_API_QUERY;
  private static final String SECOND_LVL_JOB_QUERY = "/job/job/" + Constants.XML_API_QUERY;
  private static final String THIRD_LVL_A_FOLDER_QUERY = "/job/folder/job/aFolder/" + Constants.XML_API_QUERY;
  private static final String THIRD_LVL_A_FREESTYLE_JOB_IN_FOLDER_QUERY = "/job/folder/job/aFreestyleJobInFolder/" + Constants.XML_API_QUERY;
  private static final String THIRD_LVL_A_MAVEN_JOB_IN_FOLDER_QUERY = "/job/folder/job/aMavenJobInFolder/" + Constants.XML_API_QUERY;
  private static final String FOURTH_LVL_B_FREESTYLE_JOB_IN_A_FOLDER_QUERY = "/job/folder/job/aFolder/job/bFreestyleJobInFolder/" + Constants.XML_API_QUERY;
  private static final String FOURTH_LVL_B_MAVEN_JOB_IN_A_FOLDER_QUERY = "/job/folder/job/aFolder/job/bMavenJobInFolder/" + Constants.XML_API_QUERY;

  private static final String EMPTY_FREESTYLE_JOB_BODY = "<freeStyleProject/>";
  private static final String EMPTY_MAVEN_JOB_BODY = "<mavenModuleSet/>";

  private static final String FOLDER_CONFIG_QUERY = "/job/folder//config.xml";
  private static final String JOB_CONFIG_QUERY = "/job/job//config.xml";
  private static final String A_FOLDER_CONFIG_QUERY = "/job/folder/job/aFolder//config.xml";
  private static final String A_FREESTYLE_JOB_IN_FOLDER_CONFIG_QUERY = "/job/folder/job/aFreestyleJobInFolder//config.xml";
  private static final String A_MAVEN_JOB_IN_FOLDER_CONFIG_QUERY = "/job/folder/job/aMavenJobInFolder//config.xml";
  private static final String B_FREESTYLE_JOB_IN_FOLDER_CONFIG_QUERY = "/job/folder/job/aFolder/job/bFreestyleJobInFolder//config.xml";
  private static final String B_MAVEN_JOB_IN_FOLDER_CONFIG_QUERY = "/job/folder/job/aFolder/job/bMavenJobInFolder//config.xml";

  private static final String FOLDER_CONFIG = "folder" + separator + "config.xml";
  private static final String JOB_CONFIG = "job" + separator + "config.xml";
  private static final String A_FOLDER_CONFIG = "folder" + separator + "jobs" + separator + "aFolder" + separator + "config.xml";
  private static final String A_FREESTYLE_JOB_IN_FOLDER_CONFIG = "folder" + separator + "jobs" + separator + "aFreestyleJobInFolder" + separator + "config.xml";
  private static final String A_MAVEN_JOB_IN_FOLDER_CONFIG = "folder" + separator + "jobs" + separator + "aMavenJobInFolder" + separator + "config.xml";
  private static final String B_FREESTYLE_JOB_IN_FOLDER_CONFIG = "folder" + separator + "jobs" + separator + "aFolder" + separator + "jobs" + separator + "bFreestyleJobInFolder" + separator + "config.xml";
  private static final String B_MAVEN_JOB_IN_FOLDER_CONFIG = "folder" + separator + "jobs" + separator + "aFolder" + separator + "jobs" + separator + "bMavenJobInFolder" + separator + "config.xml";

  private final String url;

  RemoteJenkins(int port) {
    this.url = BASE_URL + ":" + port;

    stubQueryResponses();
    stubImportResponses();
  }

  private void stubQueryResponses() {
    stubTopLevelResponse();
    stubSecondLevelResponse();
    stubThirdLevelResponse();
    stubFourthLevelResponse();
  }

  private void stubImportResponses() {
    stubTopLevelConfigResponse();
    stubSecondLevelConfigResponse();
    stubThirdLevelConfigResponse();
  }

  private void stubTopLevelResponse() {
    stubGetRequest(TOP_LVL_QUERY, getResponse(TOP_LVL_RSP_XML));
  }

  private void stubSecondLevelResponse() {
    stubGetRequest(SECOND_LVL_FOLDER_QUERY, getResponse(SECOND_LVL_RSP_XML));
    stubGetRequest(SECOND_LVL_JOB_QUERY, EMPTY_FREESTYLE_JOB_BODY);
  }

  private void stubThirdLevelResponse() {
    stubGetRequest(THIRD_LVL_A_FOLDER_QUERY, getResponse(THIRD_LVL_RSP_XML));
    stubGetRequest(THIRD_LVL_A_FREESTYLE_JOB_IN_FOLDER_QUERY, EMPTY_FREESTYLE_JOB_BODY);
    stubGetRequest(THIRD_LVL_A_MAVEN_JOB_IN_FOLDER_QUERY, EMPTY_MAVEN_JOB_BODY);
  }

  private void stubFourthLevelResponse() {
    stubGetRequest(FOURTH_LVL_B_FREESTYLE_JOB_IN_A_FOLDER_QUERY, EMPTY_FREESTYLE_JOB_BODY);
    stubGetRequest(FOURTH_LVL_B_MAVEN_JOB_IN_A_FOLDER_QUERY, EMPTY_MAVEN_JOB_BODY);
  }

  private void stubTopLevelConfigResponse() {
    stubGetRequest(FOLDER_CONFIG_QUERY, getResponse(FOLDER_CONFIG));
    stubGetRequest(JOB_CONFIG_QUERY, getResponse(JOB_CONFIG));
  }

  private void stubSecondLevelConfigResponse() {
    stubGetRequest(A_FOLDER_CONFIG_QUERY, getResponse(A_FOLDER_CONFIG));
    stubGetRequest(A_FREESTYLE_JOB_IN_FOLDER_CONFIG_QUERY, getResponse(A_FREESTYLE_JOB_IN_FOLDER_CONFIG));
    stubGetRequest(A_MAVEN_JOB_IN_FOLDER_CONFIG_QUERY, getResponse(A_MAVEN_JOB_IN_FOLDER_CONFIG));
  }

  private void stubThirdLevelConfigResponse() {
    stubGetRequest(B_FREESTYLE_JOB_IN_FOLDER_CONFIG_QUERY, getResponse(B_FREESTYLE_JOB_IN_FOLDER_CONFIG));
    stubGetRequest(B_MAVEN_JOB_IN_FOLDER_CONFIG_QUERY, getResponse(B_MAVEN_JOB_IN_FOLDER_CONFIG));
  }

  private void stubGetRequest(String url, String template) {
    stubFor(get(urlEqualTo(url))
        .willReturn(aResponse()
            .withStatus(200)
            .withBody(template)));
  }

  String getUrl() {
    return url;
  }

  void verifyQueried() {
    verifyGetRequest(TOP_LVL_QUERY);
  }

  void verifyQueriedRecursive() {
    verifyGetRequest(TOP_LVL_QUERY);
    verifyGetRequest(SECOND_LVL_FOLDER_QUERY);
    verifyGetRequest(THIRD_LVL_A_FOLDER_QUERY);
  }

  void verifyImported() {
    verifyGetRequest(FOLDER_CONFIG_QUERY);
    verifyGetRequest(JOB_CONFIG_QUERY);
  }

  void verifyImportedRecursive() {
    verifyGetRequest(FOLDER_CONFIG_QUERY);
    verifyGetRequest(JOB_CONFIG_QUERY);
    verifyGetRequest(A_FOLDER_CONFIG_QUERY);
    verifyGetRequest(A_FREESTYLE_JOB_IN_FOLDER_CONFIG_QUERY);
    verifyGetRequest(A_MAVEN_JOB_IN_FOLDER_CONFIG_QUERY);
    verifyGetRequest(B_FREESTYLE_JOB_IN_FOLDER_CONFIG_QUERY);
    verifyGetRequest(B_MAVEN_JOB_IN_FOLDER_CONFIG_QUERY);
  }
  
  private void verifyGetRequest(String url) {
    verify(getRequestedFor(urlEqualTo(url)));
  }

  private static final String FIELD_START = "\\$\\{";
  private static final String FIELD_END = "\\}";
  private static final String REGEX = FIELD_START + "(baseUrl)" + FIELD_END;

  private String getResponse(String template) {
    String rawXml = templateToString(template);
    return rawXml.replaceAll(REGEX, url);
  }

  private String templateToString(String template) {
    try {
      return IOUtils.toString(Thread.currentThread().getContextClassLoader().getResourceAsStream(template));
    } catch (IOException e) {
      throw new RemoteJenkinsIOException(e.getMessage());
    }
  }

  private static final class RemoteJenkinsIOException extends RuntimeException {
    RemoteJenkinsIOException(String message) {
      super(message);
    }
  }
}
