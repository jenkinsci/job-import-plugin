package org.jenkins.ci.plugins.jobimport;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.html.*;
import org.jenkins.ci.plugins.jobimport.utils.Constants;
import org.jvnet.hudson.test.JenkinsRule;


/**
 * Created by evildethow on 30/06/2016.
 */
final class JobImportClient {

  private static final int IMPORT_SUBMIT_RETRY = 5;
  private static final long IMPORT_SUBMIT_RETRY_WAIT_TIME = 1000L;

  private HtmlPage currentPage;

  JobImportClient(JenkinsRule.WebClient webClient) throws Exception {
    this.currentPage = webClient.goTo(Constants.URL_NAME);
  }

  void doQuerySubmit(String remoteUrl, boolean recursiveSearch) throws Exception {
    HtmlInput remoteUrlInput = (HtmlInput) currentPage.getElementsByName(Constants.REMOTE_URL_PARAM).get(0);
    remoteUrlInput.setValueAttribute(remoteUrl);

    HtmlCheckBoxInput recursiveSearchInput = (HtmlCheckBoxInput) currentPage.getElementsByName(Constants.RECURSIVE_PARAM).get(0);
    recursiveSearchInput.setChecked(recursiveSearch);

    HtmlForm form = currentPage.getFormByName("query");
    currentPage = form.getInputByValue("Query!").click();
  }

  void selectJobs() {
    for (DomElement checkBox : currentPage.getElementsByName(Constants.JOB_URL_PARAM)) {
      ((HtmlCheckBoxInput) checkBox).setChecked(true);
    }
  }

  void doImportSubmit() throws Exception {
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
