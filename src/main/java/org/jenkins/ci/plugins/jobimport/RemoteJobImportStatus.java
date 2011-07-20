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

/**
 * @author <a href="mailto:jieryn@gmail.com">Jesse Farinacci</a>
 * @since 1.0
 */
public final class RemoteJobImportStatus implements Comparable<RemoteJobImportStatus> {
  private RemoteJob remoteJob;
  private String    status;

  public RemoteJobImportStatus() {
    this((RemoteJob) null, (String) null);
  }

  public RemoteJobImportStatus(final RemoteJob remoteJob) {
    this(remoteJob, (String) null);
  }

  public RemoteJobImportStatus(final RemoteJob remoteJob, final String status) {
    super();
    this.remoteJob = remoteJob;
    this.status = status;
  }

  public RemoteJob getRemoteJob() {
    return remoteJob;
  }

  public void setRemoteJob(final RemoteJob remoteJob) {
    this.remoteJob = remoteJob;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(final String status) {
    this.status = status;
  }

  public int compareTo(final RemoteJobImportStatus other) {
    if (this == other) {
      return 0;
    }

    return remoteJob.compareTo(other.getRemoteJob());
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }

    if (!(obj instanceof RemoteJobImportStatus)) {
      return false;
    }

    return remoteJob.equals(((RemoteJobImportStatus) obj).getRemoteJob());
  }

  @Override
  public int hashCode() {
    return remoteJob.hashCode();
  }

  @Override
  public String toString() {
    return new StringBuilder().append("RemoteJobImportStatus: ").append(remoteJob).append(", ").append(status)
        .toString();
  }
}
