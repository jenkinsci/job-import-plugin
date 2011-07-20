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
import hudson.util.FormValidation;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.lang.builder.ToStringBuilder;
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

  private static final Logger LOG = Logger.getLogger(JobImportAction.class.getName());

  public static void main(final String[] args) {
    try {
      System.out.println(RemoteJobUtils.fromXml(URLUtils.fetchUrl("http://localhost:8080/view/ViewName/api/xml")));
    }

    catch (final Exception e) {
      e.printStackTrace();
    }
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  public void doImport(final StaplerRequest request, final StaplerResponse response) throws ServletException,
      IOException {
    LOG.info("doImport(request, response)");

    for (final Map.Entry parameter : (Set<Map.Entry>) request.getParameterMap().entrySet()) {
      LOG.info(parameter.getKey() + " -> " + ToStringBuilder.reflectionToString(parameter.getValue()));
    }

    if (request.hasParameter("jobUrl")) {
      final SortedSet<String> jobUrls = new TreeSet<String>(Arrays.asList(request.getParameterValues("jobUrl")));

    }

    response.forwardToPreviousPage(request);
  }

  public void doQuery(final StaplerRequest request, final StaplerResponse response) throws ServletException,
      IOException {
    LOG.info("doQuery(request, response)");
    LOG.info(request.getParameter("remoteUrl"));

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

  public SortedSet<RemoteJob> getRemoteJobs(/*
                                             * @QueryParameter("remoteUrl")
                                             * final String remoteUrl
                                             */) throws XPathExpressionException, MalformedURLException, SAXException,
      IOException, ParserConfigurationException {
    // final SortedSet<RemoteJob> remoteJobs = new TreeSet<RemoteJob>();
    //
    // remoteJobs.add(new RemoteJob("Job 1",
    // "http://ci.jenkins-ci.org/job/Job1", "Job 1 is an example job."));
    // remoteJobs.add(new RemoteJob("Job 2",
    // "http://ci.jenkins-ci.org/job/Job2", "Job 2 is an example job."));
    // remoteJobs.add(new RemoteJob("Job 3",
    // "http://ci.jenkins-ci.org/job/Job3", "Job 3 is an example job."));
    //
    // return remoteJobs;

    LOG.info("getRemoteJobs(" /* + remoteUrl */+ ")");
    return RemoteJobUtils.fromXml(URLUtils.fetchUrl(/* remoteUrl + */"/api/xml"));
  }

  public String getUrlName() {
    return "/job-import";
  }

  public boolean isRemoteJobsAvailable() {
    return true;// getRemoteJobs().size() > 0;
  }
}
