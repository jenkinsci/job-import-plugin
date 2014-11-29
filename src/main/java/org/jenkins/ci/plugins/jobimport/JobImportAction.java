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

import hudson.Extension;
import hudson.model.Hudson;
import hudson.model.RootAction;
import hudson.model.TopLevelItem;
import hudson.util.FormValidation;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @author <a href="mailto:jieryn@gmail.com">Jesse Farinacci</a>
 * @since 1.0
 */
@Extension
public final class JobImportAction implements RootAction {

  private static final Logger                               LOG                    = Logger
                                                                                       .getLogger(JobImportAction.class
                                                                                           .getName());

  private String                                            remoteUrl;
  private String username, password;

  private final SortedSet<RemoteJob>                        remoteJobs             = new TreeSet<RemoteJob>();

  private final SortedMap<RemoteJob, RemoteJobImportStatus> remoteJobsImportStatus = new TreeMap<RemoteJob, RemoteJobImportStatus>();

  public void doClear(final StaplerRequest request, final StaplerResponse response) throws ServletException,
      IOException {
    remoteUrl = null;
    username = password = null;
    remoteJobs.clear();
    remoteJobsImportStatus.clear();
    response.sendRedirect(Hudson.getInstance().getRootUrl());
  }

  public void doImport(final StaplerRequest request, final StaplerResponse response) throws ServletException,
      IOException {
    remoteJobsImportStatus.clear();

    if (isRemoteJobsAvailable()) {
      if (request.hasParameter("jobUrl")) {
        for (final String jobUrl : Arrays.asList(request.getParameterValues("jobUrl"))) {
          final RemoteJob remoteJob = getRemoteJobs(jobUrl);
          if (remoteJob != null) {
            if (!remoteJobsImportStatus.containsKey(remoteJob)) {
              remoteJobsImportStatus.put(remoteJob, new RemoteJobImportStatus(remoteJob));
            }

            // ---

            if (Hudson.getInstance().getItem(remoteJob.getName()) != null) {
              remoteJobsImportStatus.get(remoteJob).setStatus(MessagesUtils.formatFailedDuplicateJobName());
            }

            else {
              InputStream inputStream = null;

              try {
                  inputStream = URLUtils.fetchUrl(remoteJob.getUrl() + "/config.xml", username, password);
                  Hudson.getInstance().createProjectFromXML(remoteJob.getName(), inputStream);
                  remoteJobsImportStatus.get(remoteJob).setStatus(MessagesUtils.formatSuccess());
              }

              catch (final Exception e) {
                LOG.warning("Job Import Failed: " + e.getMessage());
                if (LOG.isLoggable(Level.INFO)) {
                  LOG.log(Level.INFO, e.getMessage(), e);
                }

                // ---

                remoteJobsImportStatus.get(remoteJob).setStatus(MessagesUtils.formatFailedException(e));

                try {
                    TopLevelItem created = Hudson.getInstance().getItem(remoteJob.getName());
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
      }
    }

    response.forwardToPreviousPage(request);
  }

  public void doQuery(final StaplerRequest request, final StaplerResponse response) throws ServletException,
      IOException {
    remoteJobs.clear();
    remoteJobsImportStatus.clear();
    remoteUrl = request.getParameter("remoteUrl");
    username = request.getParameter("username");
    password = request.getParameter("password");

    try {
      if (StringUtils.isNotEmpty(remoteUrl)) {
          Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(URLUtils.fetchUrl(remoteUrl + "/api/xml?tree=jobs[name,url,description]", username, password));
          NodeList nl = doc.getElementsByTagName("job");
          for (int i = 0; i < nl.getLength(); i++) {
              Element job = (Element) nl.item(i);
              String desc = text(job, "description");
              remoteJobs.add(new RemoteJob(text(job, "name"), text(job, "url"), desc != null ? desc : ""));
          }
      }
    }

    catch (Exception e) {
        LOG.log(Level.SEVERE, (new StringBuilder()).append("Failed to import job from remote ").append(remoteUrl).toString(), e);
    }

    response.forwardToPreviousPage(request);
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

  public String getDisplayName() {
    return Messages.DisplayName();
  }

  public String getIconFileName() {
    return "/images/32x32/setting.png";
  }

  public SortedSet<RemoteJob> getRemoteJobs() {
    return remoteJobs;
  }

  private RemoteJob getRemoteJobs(final String jobUrl) {
    if (StringUtils.isNotEmpty(jobUrl)) {
      for (final RemoteJob remoteJob : remoteJobs) {
        if (jobUrl.equals(remoteJob.getUrl())) {
          return remoteJob;
        }
      }
    }

    return null;
  }

  public SortedMap<RemoteJob, RemoteJobImportStatus> getRemoteJobsImportStatus() {
    return remoteJobsImportStatus;
  }

  public String getRemoteUrl() {
    return remoteUrl;
  }
    public String getUsername() {
        return username;
    }
    public String getPassword() {
        return password;
    }

  public String getUrlName() {
    return "/job-import";
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
}
