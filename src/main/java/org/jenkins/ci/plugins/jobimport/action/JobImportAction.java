/*
 * The MIT License
 * 
 * Copyright (c) 2011, Jesse Farinacci, Manufacture Francaise des Pneumatiques Michelin,
 * Daniel Petisme, Romain Seguy
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

package org.jenkins.ci.plugins.jobimport.action;

import hudson.Extension;
import hudson.Functions;
import hudson.model.Hudson;
import hudson.model.Item;
import hudson.model.RootAction;
import hudson.security.Permission;
import hudson.security.PermissionGroup;
import hudson.util.FormValidation;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jenkins.ci.plugins.jobimport.JobImportPlugin;
import org.jenkins.ci.plugins.jobimport.model.JenkinsInstance;
import org.jenkins.ci.plugins.jobimport.model.JobImportRequest;
import org.jenkins.ci.plugins.jobimport.model.RemoteJob;
import org.jenkins.ci.plugins.jobimport.model.RemoteJobImportStatus;
import org.jenkins.ci.plugins.jobimport.util.JenkinsAPIUtils;
import org.jenkins.ci.plugins.jobimport.util.MessagesUtils;
import org.jenkins.ci.plugins.jobimport.util.RemoteJobUtils;
import org.jenkins.ci.plugins.jobimport.util.URLUtils;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.xml.sax.SAXException;

import javax.servlet.ServletException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:jieryn@gmail.com">Jesse Farinacci</a>
 * @since 1.0
 */
@Extension
public final class JobImportAction implements RootAction {

  public static final PermissionGroup PERMISSIONS = new PermissionGroup(JobImportAction.class, Messages._Job_Import_Plugin_Import_Permissions_Title());

  // controls the import of jobs in the current Jenkins instance
  public static final Permission IMPORT = new Permission(PERMISSIONS, "Import", Messages._Job_Import_Plugin_Import_Permissions_Description(), null);

  // gets all the jobs in another Jenkins instance with an XPath filter
  private static final String REQUEST_URL_FORMAT = "{0}/api/xml{1}";

  private static final Logger                               LOG                    = Logger
                                                                                       .getLogger(JobImportAction.class
                                                                                           .getName());

  private String                                            remoteUrl;

  private String jobFilterPattern;

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

    if (isRemoteJobsAvailable() && request.hasParameter("job.url")) {
        // get the Jenkins the use once for all
        JenkinsInstance instance = new JenkinsInstance(remoteUrl, "", "", "");
        if (getPlugin().isEnableJenkinsList()) {
          instance = (JenkinsInstance) CollectionUtils.find(getPlugin().getJenkinsInstances(), new Predicate() {
            public boolean evaluate(Object object) {
              return ((JenkinsInstance) object).getUrl().equalsIgnoreCase(remoteUrl);
            }
          });
        }

        // get all requests into an ad-hoc list
        List<JobImportRequest> jobImportRequests = request.bindParametersToList(JobImportRequest.class, "job.");

        // for each request
        for (final JobImportRequest jobImportRequest : jobImportRequests) {
          final RemoteJob remoteJob = getRemoteJobs(jobImportRequest.getUrl());

          if (remoteJob != null) {
            if (!remoteJobsImportStatus.containsKey(remoteJob)) {
              remoteJobsImportStatus.put(remoteJob, new RemoteJobImportStatus(remoteJob));
            }

            String importedJobName = remoteJob.getName();
            if (StringUtils.isNotBlank(jobImportRequest.getNewName())) {
              importedJobName = jobImportRequest.getNewName();
            }

            // does the job already exist?
            if (Hudson.getInstance().getItem(importedJobName) != null) {
              remoteJobsImportStatus.get(remoteJob).setStatus(MessagesUtils.formatFailedDuplicateJobName());
            }
            else {
              InputStream inputStream = null;

              try {
                String configXml = JenkinsAPIUtils.getResource(instance, remoteJob.getUrl() + "/config.xml");

                // is there a pattern to match for replacement purposes?
                if (StringUtils.isNotBlank(jobImportRequest.getPatternToMatch())) {
                  Pattern patternToMatch = Pattern.compile(jobImportRequest.getPatternToMatch());
                  Matcher matcher = patternToMatch.matcher(configXml);
                  StringBuffer sb = new StringBuffer();

                  while (matcher.find()) {
                    matcher.appendReplacement(sb, jobImportRequest.getNewValue());
                  }

                  matcher.appendTail(sb);
                  configXml = sb.toString();
                }

                if (isValidProject(configXml)) {
                  inputStream = IOUtils.toInputStream(configXml);
                  Hudson.getInstance().createProjectFromXML(importedJobName, inputStream);
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

                remoteJobsImportStatus.get(remoteJob).setStatus(MessagesUtils.formatFailedException(e));

                try {
                  if (Hudson.getInstance().getItem(importedJobName) != null) {
                    Hudson.getInstance().getItem(importedJobName).delete();
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

    return false;
  }

  public void doQuery(final StaplerRequest request, final StaplerResponse response) throws ServletException,
      IOException {
    remoteJobs.clear();
    remoteJobsImportStatus.clear();
    remoteUrl = request.getParameter("remoteUrl");
    jobFilterPattern = request.getParameter("jobFilterPattern");

    try {
      if (StringUtils.isNotEmpty(remoteUrl)) {
        // filter the incoming list with the jobFilterPattern pattern (defined in the Jenkins instance)
        JobImportPlugin plugin = getPlugin();
        String adminFilterPattern = "";

        if (plugin != null) {
          if (plugin.isEnableJenkinsList()) {
            JenkinsInstance instance = (JenkinsInstance) CollectionUtils.find(getPlugin().getJenkinsInstances(), new Predicate() {
              public boolean evaluate(Object object) {
                return ((JenkinsInstance) object).getUrl().equalsIgnoreCase(remoteUrl);
              }
            });

            if (instance != null) {
              adminFilterPattern = instance.getJobFilter();
            }
          }
        }

        String xpathFilters = StringUtils.trimToEmpty(buildXpathFilters(adminFilterPattern, jobFilterPattern));

        final String requestUrl = MessageFormat.format(REQUEST_URL_FORMAT, remoteUrl, xpathFilters);

        SortedSet<RemoteJob> loaded = RemoteJobUtils.fromXml(URLUtils.fetchUrl(requestUrl));

        remoteJobs.addAll(loaded);
      }
    } catch (final XPathExpressionException e) {
      LOG.log(Level.WARNING, "XPath issue", e);
    } catch (final MalformedURLException e) {
      LOG.log(Level.WARNING, "URL issue", e);
    } catch (final SAXException e) {
      LOG.log(Level.WARNING, "SAX issue", e);
    } catch (final IOException e) {
      LOG.log(Level.WARNING, "IO issue", e);
    } catch (final ParserConfigurationException e) {
      LOG.log(Level.WARNING, "Parser issue", e);
    }

    response.forwardToPreviousPage(request);
  }

  /**
   * Builds the filter to apply to the XPath request.
   * 
   * <p>The filter is built using:<ol>
   * <li>The <a href="https://issues.jenkins-ci.org/browse/JENKINS-626">xpath attribute</a> of Jenkins' XML API.</li>
   * <li>XPath's {@code contains} function (e.g. {@code http://localhost:8080/api/xml?xpath=/hudson/job[contains(name, 'Build')]}).</li>
   * <li>The tricky {@code exclude} parameter provided by the {@link hudson.model.Api#doXml(org.kohsuke.stapler.StaplerRequest, org.kohsuke.stapler.StaplerResponse, String, String, int)} method.</li>
   * </ol></p>
   * <p>Cf. <a href="https://wiki.jenkins-ci.org/display/JENKINS/Remote+access+API#RemoteaccessAPI-XPathexclusion">Jenkins' wiki</a>
   * for more details, and also {@code http://.../jenkins/api/}.</p>
   *
   * @param adminFilterPattern The global defined pattern to search
   * @param jobFilterPattern   The local defined pattern to search
   */
  private String buildXpathFilters(String adminFilterPattern, String jobFilterPattern) {
    StringBuilder filterBuilder = new StringBuilder("");

    List<String> xpathRequestFilters = new ArrayList<String>();

    String sXpathFilter = "";
    sXpathFilter = buildXpathFilters(adminFilterPattern);

    if (StringUtils.isNotBlank(sXpathFilter)) {
      xpathRequestFilters.add(sXpathFilter);
    }

    sXpathFilter = "";
    sXpathFilter = buildXpathFilters(jobFilterPattern);

    if (StringUtils.isNotBlank(sXpathFilter)) {
      xpathRequestFilters.add(sXpathFilter);
    }

    //escape spaces by %20
    String sfilters = StringUtils.join(xpathRequestFilters, "%20or%20");

    if (StringUtils.isNotBlank(sfilters)) {
      filterBuilder.append("?exclude=//job[");
      filterBuilder.append(sfilters);
      filterBuilder.append("]");
    }

    return filterBuilder.toString();
  }

   /**
    * build the actual {@code xpath contains} filter
    *
    * @param filterValue
    * @return
    */
  private String buildXpathFilters(String filterValue) {
    StringBuilder filtersBuilder = new StringBuilder("");

    if (StringUtils.isNotBlank(filterValue)) {
      filtersBuilder.append("not(contains(name,'");
      filtersBuilder.append(filterValue);
      filtersBuilder.append("'))");
    }
    return filtersBuilder.toString();
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

  public String getJobFilterPattern() {
    return jobFilterPattern;
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

  public void setJobFilterPattern(String jobFilterPattern) {
    this.jobFilterPattern = jobFilterPattern;
  }

  public JobImportPlugin getPlugin() {
    return Hudson.getInstance().getPlugin(JobImportPlugin.class);
  }

  /**
   * Check if the current user has the {@link Item.CREATE} and {@link JobImportAction.IMPORT} permission
   *
   * @throws IOException
   * @throws ServletException trigger the login redirection
   */
  public void checkCreateAndImportPermissions() throws IOException, ServletException {
    Functions.checkPermission(this, IMPORT);
    Functions.checkPermission(this, Item.CREATE);
  }

}
