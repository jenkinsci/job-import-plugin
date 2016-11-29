package org.jenkins.ci.plugins.jobimport;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

/**
 * Created by evildethow on 28/06/2016.
 */
final class RemoteJobUtils {

  private RemoteJobUtils() {
    throw new UnsupportedOperationException("Cannot instantiate utility class");
  }

  static String fullName(RemoteJob remoteJob) {
    if (remoteJob == null) {
      return "";
    }

    if (!remoteJob.hasParent()) {
      return remoteJob.getName();
    }

    return StringUtils.substringBeforeLast(fullName(remoteJob, ""), "jobs/");
  }

  private static String fullName(RemoteJob remoteJob, String name) {
    name = remoteJob.getName() + "/jobs/" + name;
    return remoteJob.hasParent() ? fullName(remoteJob.getParent(), name) : name;
  }

  private static final int MAX_STR_LEN = 4096;

  static String cleanRemoteString(final String string) {
    return StringUtils.substring(StringEscapeUtils.escapeHtml(string), 0, MAX_STR_LEN);
  }

  static RemoteJob getRemoteJob(SortedSet<RemoteJob> remoteJobs, String jobUrl) {
    List<RemoteJob> matches  = new ArrayList<RemoteJob>();

    findRemoteJob(remoteJobs, jobUrl, matches);

    return matches.isEmpty() ? null : matches.get(0);
  }

  private static void findRemoteJob(SortedSet<RemoteJob> remoteJobs, String jobUrl, List<RemoteJob> matches) {
    if (StringUtils.isNotEmpty(jobUrl) && matches.isEmpty()) {
      for (final RemoteJob remoteJob : remoteJobs) {
        if (jobUrl.trim().equals(remoteJob.getUrl().trim())) {
          matches.add(remoteJob);
        } else if (remoteJob.hasChildren()) {
          findRemoteJob(remoteJob.getChildren(), jobUrl, matches);
        }
      }
    }
  }
}
