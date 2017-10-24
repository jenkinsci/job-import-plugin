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

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernameListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Item;
import hudson.model.RootAction;
import hudson.model.TopLevelItem;
import hudson.security.ACL;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jenkins.ci.plugins.jobimport.client.RestApiClient;
import org.jenkins.ci.plugins.jobimport.model.RemoteFolder;
import org.jenkins.ci.plugins.jobimport.model.RemoteItem;
import org.jenkins.ci.plugins.jobimport.utils.Constants;
import org.jenkins.ci.plugins.jobimport.utils.CredentialsUtils;
import org.jenkins.ci.plugins.jobimport.utils.CredentialsUtils.NullSafeCredentials;
import org.jenkins.ci.plugins.jobimport.utils.RemoteItemUtils;
import org.jenkins.ci.plugins.jobimport.utils.URLUtils;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.jenkins.ci.plugins.jobimport.utils.CredentialsUtils.allCredentials;

/**
 * @author <a href="mailto:jieryn@gmail.com">Jesse Farinacci</a>
 * @since 1.0
 */
@Extension
public final class JobImportAction implements RootAction, Describable<JobImportAction> {

  private static final Logger LOG = Logger.getLogger(JobImportAction.class.getName());

  private String remoteUrl;
  private String localFolder;
  private String credentialId;
  private String recursiveSearch;

  private final SortedSet<RemoteItem> remoteJobs = new TreeSet<RemoteItem>();
  private final SortedMap<RemoteItem, RemoteItemImportStatus> remoteJobsImportStatus = new TreeMap<RemoteItem, RemoteItemImportStatus>();

  public void doClear(final StaplerRequest request, final StaplerResponse response) throws ServletException,
      IOException {
    remoteUrl = null;
    credentialId = null;
    localFolder = null;
    recursiveSearch = null;
    remoteJobs.clear();
    remoteJobsImportStatus.clear();
    response.sendRedirect(Jenkins.getActiveInstance().getRootUrl());
  }

  public void doImport(final StaplerRequest request, final StaplerResponse response) throws ServletException,
      IOException {
    remoteJobsImportStatus.clear();

    localFolder = request.getParameter(Constants.LOCAL_FOLDER_PARAM);

    if (isRemoteJobsAvailable()) {
      if (request.hasParameter(Constants.JOB_URL_PARAM)) {
        for (final String jobUrl : Arrays.asList(request.getParameterValues(Constants.JOB_URL_PARAM))) {
          doImportInternal(jobUrl, localFolder);
        }
      }
    }

    response.forwardToPreviousPage(request);
  }

  private void doImportInternal(String jobUrl, String localPath) throws IOException {
    final RemoteItem remoteJob = RemoteItemUtils.getRemoteJob(remoteJobs, jobUrl);
    if (remoteJob != null) {
      if (!remoteJobsImportStatus.containsKey(remoteJob)) {
        remoteJobsImportStatus.put(remoteJob, new RemoteItemImportStatus(remoteJob));
      }

      // ---

      if (StringUtils.isNotEmpty(localPath) && Jenkins.getActiveInstance().getItemByFullName(localPath + remoteJob.getName()) != null) {
        remoteJobsImportStatus.get(remoteJob).setStatus(MessagesUtils.formatFailedDuplicateJobName());
      } else if (StringUtils.isEmpty(localPath) && Jenkins.getActiveInstance().getItem(remoteJob.getName()) != null) {
        remoteJobsImportStatus.get(remoteJob).setStatus(MessagesUtils.formatFailedDuplicateJobName());
      } else {
        InputStream inputStream = null;

        NullSafeCredentials credentials = CredentialsUtils.getCredentials(credentialId);

        try {
          inputStream = URLUtils.fetchUrl(remoteJob.getUrl() + "/config.xml", credentials.username, credentials.password);

          final Item newItem;
          if (StringUtils.isNotEmpty(localPath)) {
            newItem = Jenkins.getActiveInstance().getItemByFullName(localPath, com.cloudbees.hudson.plugins.folder.Folder.class).
                    createProjectFromXML(remoteJob.getFullName(), inputStream);
          } else {
            newItem = Jenkins.getActiveInstance().
                    createProjectFromXML(remoteJob.getFullName(), inputStream);
          }

          if (newItem != null) {
            newItem.save();
          }

          remoteJobsImportStatus.get(remoteJob).setStatus(MessagesUtils.formatSuccess());

          if (remoteJob.isFolder() && ((RemoteFolder)remoteJob).hasChildren()) {
            for (RemoteItem childJob : ((RemoteFolder)remoteJob).getChildren()) {
              doImportInternal(childJob.getUrl(), newItem.getFullName());
            }
          }
          /*
          try{
            Jenkins instance= Jenkins.getActiveInstance();
            instance.checkPermission(instance.ADMINISTER);
            Jenkins.getActiveInstance().doReload();
          } catch(AccessDeniedException2 ex2){
            remoteJobsImportStatus.get(remoteJob).setStatus(MessagesUtils.formatSuccessNotReloaded());
            LOG.log(Level.INFO, "Failed to reload Jenkins config because the user lacks the Overall Administer permission");
          }
          */
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
    remoteUrl = request.getParameter(Constants.REMOTE_URL_PARAM);
    credentialId = request.getParameter("_.credentialId");

    recursiveSearch = request.getParameter(Constants.RECURSIVE_PARAM);

    doQueryInternal(null, remoteUrl, CredentialsUtils.getCredentials(credentialId));

    response.forwardToPreviousPage(request);
  }

  private void doQueryInternal(RemoteFolder parent, String url, NullSafeCredentials credentials) {
    remoteJobs.addAll(RestApiClient.getRemoteItems(parent, url, credentials, isRecursive(recursiveSearch)));
  }

  private boolean isRecursive(String param) {
    return StringUtils.equals("on", param);
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

  public SortedSet<RemoteItem> getRemoteJobs() {
    return remoteJobs;
  }

  public SortedMap<RemoteItem, RemoteItemImportStatus> getRemoteJobsImportStatus() {
    return remoteJobsImportStatus;
  }

  public String getRemoteUrl() {
    return remoteUrl;
  }

  public String getUrlName() {
    return "/" + Constants.URL_NAME;
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

  public String getLocalFolder() {
    return localFolder;
  }

  public void setLocalFolder(String localFolder) {
    this.localFolder = localFolder;
  }

  @Override
  public Descriptor<JobImportAction> getDescriptor() {
    return Jenkins.getActiveInstance().getDescriptorOrDie(getClass());
  }
  
  @Extension
  public static final class JobImportActionDescriptor extends Descriptor<JobImportAction> {

    @Override
    public String getDisplayName() { return ""; }

    public ListBoxModel doFillCredentialIdItems() {
      return new StandardListBoxModel()
              .includeEmptyValue()
              .includeMatchingAs(
                      ACL.SYSTEM,
                      (Item) null,
                      StandardUsernamePasswordCredentials.class,
                      Collections.<DomainRequirement>emptyList(),
                      CredentialsMatchers.always()
              );

    }
  }
}
