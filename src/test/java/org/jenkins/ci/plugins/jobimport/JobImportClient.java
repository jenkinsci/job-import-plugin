package org.jenkins.ci.plugins.jobimport;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.html.*;
import org.jenkins.ci.plugins.jobimport.utils.Constants;
import org.jvnet.hudson.test.JenkinsRule;


/**
 * Created by evildethow on 30/06/2016.
 */
public final class JobImportClient {

  private static final int IMPORT_SUBMIT_RETRY = 5;
  private static final long IMPORT_SUBMIT_RETRY_WAIT_TIME = 1000L;

  private HtmlPage currentPage;

  public JobImportClient(JenkinsRule.WebClient webClient) throws Exception {
    this.currentPage = webClient.goTo(Constants.URL_NAME);
  }

  public void doQuerySubmit(String remoteUrl) throws Exception {
    HtmlInput input = (HtmlInput) currentPage.getElementsByName(Constants.REMOTE_URL_PARAM).get(0);
    input.setValueAttribute(remoteUrl);
    HtmlForm form = currentPage.getFormByName("query");
    currentPage = form.getInputByValue("Query!").click();
  }

  public void selectJobs() {
    for (DomElement checkBox : currentPage.getElementsByName(Constants.JOB_URL_PARAM)) {
      ((HtmlCheckBoxInput) checkBox).setChecked(true);
    }
  }

  public void doImportSubmit() throws Exception {
    doImportSubmitWithRetry(IMPORT_SUBMIT_RETRY);
  }

  private void doImportSubmitWithRetry(int retry) throws Exception {
    try {
      HtmlForm form = currentPage.getFormByName("import");
      currentPage = form.getInputByValue("Import!").click();
    } catch (FailingHttpStatusCodeException e) {
      if (retry > 0) {
        Thread.sleep(IMPORT_SUBMIT_RETRY_WAIT_TIME);
        retry--;
        doImportSubmitWithRetry(retry);
      } else {
        throw e;
      }
    }
  }
}
