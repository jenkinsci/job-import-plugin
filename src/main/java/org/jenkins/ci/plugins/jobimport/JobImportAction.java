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

import com.cloudbees.plugins.credentials.common.StandardUsernameListBoxModel;
import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.RootAction;
import hudson.model.TopLevelItem;
import hudson.security.AccessDeniedException2;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jenkins.ci.plugins.jobimport.CredentialsUtils.NullSafeCredentials;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.servlet.ServletException;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.jenkins.ci.plugins.jobimport.CredentialsUtils.allCredentials;
import static org.jenkins.ci.plugins.jobimport.RemoteJobUtils.getRemoteJob;

/**
 * @author <a href="mailto:jieryn@gmail.com">Jesse Farinacci</a>
 * @since 1.0
 */
@Extension
public final class JobImportAction implements RootAction, Describable<JobImportAction> {

  private static final Logger LOG = Logger.getLogger(JobImportAction.class.getName());

  static final String URL_NAME= "job-import";
  static final String REMOTE_URL_PARAM = "remoteUrl";
  static final String JOB_URL_PARAM = "jobUrl";
  static final String XML_API_QUERY = "/api/xml?tree=jobs[name,url,description]";

  private String remoteUrl;
  private String credentialId;

  private final SortedSet<RemoteJob> remoteJobs = new TreeSet<RemoteJob>();
  private final SortedMap<RemoteJob, RemoteJobImportStatus> remoteJobsImportStatus = new TreeMap<RemoteJob, RemoteJobImportStatus>();

  public void doClear(final StaplerRequest request, final StaplerResponse response) throws ServletException,
      IOException {
    remoteUrl = null;
    credentialId = null;
    remoteJobs.clear();
    remoteJobsImportStatus.clear();
    response.sendRedirect(Jenkins.getActiveInstance().getRootUrl());
  }

  public void doImport(final StaplerRequest request, final StaplerResponse response) throws ServletException,
      IOException {
    remoteJobsImportStatus.clear();

    if (isRemoteJobsAvailable()) {
      if (request.hasParameter(JOB_URL_PARAM)) {
        for (final String jobUrl : Arrays.asList(request.getParameterValues(JOB_URL_PARAM))) {
          doImportInternal(jobUrl);
        }
      }
    }

    response.forwardToPreviousPage(request);
  }

  private void doImportInternal(String jobUrl) throws IOException {
    final RemoteJob remoteJob = getRemoteJob(remoteJobs, jobUrl);
    if (remoteJob != null) {
      if (!remoteJobsImportStatus.containsKey(remoteJob)) {
        remoteJobsImportStatus.put(remoteJob, new RemoteJobImportStatus(remoteJob));
      }

      // ---

      if (Jenkins.getActiveInstance().getItem(remoteJob.getName()) != null) {
        remoteJobsImportStatus.get(remoteJob).setStatus(MessagesUtils.formatFailedDuplicateJobName());
      }

      else {
        InputStream inputStream = null;

        NullSafeCredentials credentials = CredentialsUtils.getCredentials(credentialId);

        try {
          inputStream = URLUtils.fetchUrl(remoteJob.getUrl() + "/config.xml", credentials.username, credentials.password);
          Jenkins.getActiveInstance().createProjectFromXML(remoteJob.getFullName(), inputStream);
          remoteJobsImportStatus.get(remoteJob).setStatus(MessagesUtils.formatSuccess());

          if (remoteJob.hasChildren()) {
            for (RemoteJob childJob : remoteJob.getChildren()) {
              doImportInternal(childJob.getUrl());
            }
          }



          try{
            Jenkins instance= Jenkins.getActiveInstance();
            instance.checkPermission(instance.ADMINISTER);
            Jenkins.getActiveInstance().doReload();
          } catch(AccessDeniedException2 ex2){
            remoteJobsImportStatus.get(remoteJob).setStatus(MessagesUtils.formatSuccessNotReloaded());
            LOG.log(Level.INFO, "Failed to reload Jenkins config because the user lacks the Overall Administer permission");

          }
        }

        catch (final Exception e) {
          LOG.warning("Job Import Failed: " + e.getMessage());
          if (LOG.isLoggable(Level.INFO)) {
            LOG.log(Level.INFO, e.getMessage(), e);
          }
          remoteJobsImportStatus.get(remoteJob).setStatus(MessagesUtils.formatFailedException(e));

          try {
            TopLevelItem created = Jenkins.getActiveInstance().getItem(remoteJob.getName());
            if (created != null) {
              created.delete();
            }
          }
          catch (final InterruptedException e2) {
            // do nothing
          }
        }

        finally {
          IOUtils.closeQuietly(inputStream);
        }
      }
    }
  }

  public void doQuery(final StaplerRequest request, final StaplerResponse response) throws ServletException,
      IOException {
    remoteJobs.clear();
    remoteJobsImportStatus.clear();
    remoteUrl = request.getParameter(REMOTE_URL_PARAM);
    credentialId = request.getParameter("_.credentialId");

    doQueryInternal(null, remoteUrl, CredentialsUtils.getCredentials(credentialId));

    response.forwardToPreviousPage(request);
  }

  private void doQueryInternal(RemoteJob parent, String url, NullSafeCredentials credentials) {
    try {
      if (StringUtils.isNotEmpty(url)) {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(URLUtils.fetchUrl(url + XML_API_QUERY, credentials.username, credentials.password));
        NodeList nl = doc.getElementsByTagName("job");
        for (int i = 0; i < nl.getLength(); i++) {
          Element job = (Element) nl.item(i);
          String desc = text(job, "description");
          String jobUrl = text(job, "url");
          String name = text(job, "name");

          RemoteJob remoteJob;
          if (parent == null) {
            remoteJob = new RemoteJob(name, jobUrl, desc, null);
            remoteJobs.add(remoteJob);
          } else {
            remoteJob = new RemoteJob(name, jobUrl, desc, parent);
            parent.getChildren().add(remoteJob);
          }

          doQueryInternal(remoteJob, jobUrl, credentials);
        }
      }
    }

    catch (Exception e) {
      LOG.log(Level.SEVERE, (new StringBuilder()).append("Failed to import job from remote ").append(url).toString(), e);
    }
  }

  private static String text(Element e, String name) {
    NodeList nl = e.getElementsByTagName(name);
    if (nl.getLength() == 1) {
      Element e2 = (Element) nl.item(0);
      return e2.getTextContent();
    } else {
      return null;
    }
  }

  public FormValidation doTestConnection(@QueryParameter("remoteUrl") final String remoteUrl) {
    return FormValidation.ok();
  }

  public String getRootUrl() {
      return Jenkins.getActiveInstance().getRootUrl();
  }

  public String getDisplayName() {
    return Messages.DisplayName();
  }

  public String getIconFileName() {
    return "/images/32x32/setting.png";
  }

  public SortedSet<RemoteJob> getRemoteJobs() {
    return remoteJobs;
  }

  public SortedMap<RemoteJob, RemoteJobImportStatus> getRemoteJobsImportStatus() {
    return remoteJobsImportStatus;
  }

  public String getRemoteUrl() {
    return remoteUrl;
  }

  public String getUrlName() {
    return "/" + URL_NAME;
  }

  public boolean isRemoteJobsAvailable() {
    return remoteJobs.size() > 0;
  }

  public boolean isRemoteJobsImportStatusAvailable() {
    return remoteJobsImportStatus.size() > 0;
  }

  public void setRemoteUrl(final String remoteUrl) {
    this.remoteUrl = remoteUrl;
  }

  public String getCredentialId() { return credentialId; }
  


  @Override
  public Descriptor<JobImportAction> getDescriptor() {
    return Jenkins.getActiveInstance().getDescriptorOrDie(getClass());
  }
  
  @Extension
  public static final class JobImportActionDescriptor extends Descriptor<JobImportAction> {

    @Override
    public String getDisplayName() { return ""; }

    public ListBoxModel doFillCredentialIdItems() {
      return new StandardUsernameListBoxModel()
              .withEmptySelection()
              .withAll(allCredentials());
    }
  }
}
