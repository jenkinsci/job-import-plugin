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
import hudson.model.RootAction;
import hudson.model.Hudson;
import hudson.model.TopLevelItem;
import hudson.util.FormValidation;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.xml.sax.SAXException;

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

  private final SortedSet<RemoteJob>                        remoteJobs             = new TreeSet<RemoteJob>();

  private final SortedMap<RemoteJob, RemoteJobImportStatus> remoteJobsImportStatus = new TreeMap<RemoteJob, RemoteJobImportStatus>();

  public void doClear(final StaplerRequest request, final StaplerResponse response) throws ServletException,
      IOException {
    remoteUrl = null;
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
                final String configXml = URLUtils.fetchUrl(remoteJob.getUrl() + "/config.xml");

                if (isValidProject(configXml)) {
                  inputStream = IOUtils.toInputStream(configXml);
                  Hudson.getInstance().createProjectFromXML(remoteJob.getName(), inputStream);
                  remoteJobsImportStatus.get(remoteJob).setStatus(MessagesUtils.formatSuccess());
                }

                else {
                  remoteJobsImportStatus.get(remoteJob).setStatus(MessagesUtils.formatFailedNotAProject());
                }
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

  private boolean isValidProject(final String configXml) {
    if (StringUtils.isEmpty(configXml)) {
      return false;
    }

    if (configXml.contains("<project>")) {
      return true;
    }

    if (configXml.contains("<maven2-moduleset>")) {
      return true;
    }

    if (configXml.contains("<matrix-project>")) {
        return true;
      }

    return false;
  }

  public void doQuery(final StaplerRequest request, final StaplerResponse response) throws ServletException,
      IOException {
    remoteJobs.clear();
    remoteJobsImportStatus.clear();
    remoteUrl = request.getParameter("remoteUrl");

    try {
      if (StringUtils.isNotEmpty(remoteUrl)) {
        remoteJobs.addAll(RemoteJobUtils.fromXml(URLUtils.fetchUrl(remoteUrl + "/api/xml")));
      }
    }

    catch (final XPathExpressionException e) {
      // fall through
    }

    catch (final MalformedURLException e) {
      // fall through
    }

    catch (final SAXException e) {
      // fall through
    }

    catch (final IOException e) {
      // fall through
    }

    catch (final ParserConfigurationException e) {
      // fall through
    }

    response.forwardToPreviousPage(request);
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
