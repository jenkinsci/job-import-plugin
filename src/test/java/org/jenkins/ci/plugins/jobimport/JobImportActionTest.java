package org.jenkins.ci.plugins.jobimport;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

/**
 * Created by evildethow on 29/06/2016.
 */
public class JobImportActionTest {

  private static final class RecursiveSearch {
    private static final boolean ON = true;
    private static final boolean OFF = false;
  }

  //@Rule
  public JenkinsRule jenkinsRule = new JenkinsRule();

  //@Rule
  public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().dynamicPort());

  private JobImportClient client;
  private RemoteJenkins remoteJenkins;

  //@Before
  public void setUp() throws Exception {
    client = new JobImportClient(jenkinsRule.createWebClient());
    remoteJenkins = new RemoteJenkins(wireMockRule.port());
  }

  //@Test
  public void doImport() throws Exception {
    client.doQuerySubmit(remoteJenkins.getUrl(), RecursiveSearch.OFF);

    remoteJenkins.verifyQueried();

    client.selectJobs();
    client.doImportSubmit();

    remoteJenkins.verifyImported();
  }

  //@Test
  public void doImportRecursive() throws Exception {
    client.doQuerySubmit(remoteJenkins.getUrl(), RecursiveSearch.ON);

    remoteJenkins.verifyQueriedRecursive();

    client.selectJobs();
    client.doImportSubmit();

    remoteJenkins.verifyImportedRecursive();
  }
}
