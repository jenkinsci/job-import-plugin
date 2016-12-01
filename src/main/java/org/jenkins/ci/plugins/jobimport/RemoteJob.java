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

import java.util.SortedSet;
import java.util.TreeSet;

import static org.jenkins.ci.plugins.jobimport.RemoteJobUtils.cleanRemoteString;
import static org.jenkins.ci.plugins.jobimport.RemoteJobUtils.fullName;

/**
 * @author <a href="mailto:jieryn@gmail.com">Jesse Farinacci</a>
 * @since 1.0
 */
public final class RemoteJob implements Comparable<RemoteJob> {
  private final String name;
  private final String url;
  private final String description;
  private final RemoteJob parent;
  private final SortedSet<RemoteJob> children = new TreeSet<RemoteJob>();

  public RemoteJob(final String name, final String url, final String description, RemoteJob parent) {
    this.name = name;
    this.url = url;
    this.description = cleanRemoteString(description != null ? description : "");
    this.parent = parent;
  }

  public String getName() {
    return name;
  }

  public String getUrl() {
    return url;
  }

  public String getDescription() {
    return description;
  }

  public RemoteJob getParent() {
    return parent;
  }

  public SortedSet<RemoteJob> getChildren() {
    return children;
  }

  boolean hasChildren() {
    return !this.children.isEmpty();
  }

  boolean hasParent() {
    return this.parent != null;
  }

  public String getFullName() {
    return fullName(this);
  }

  @Override
  public int compareTo(final RemoteJob other) {
    if (this == other) {
      return 0;
    }

    return name.compareTo(other.getName());
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof RemoteJob && name.equals(((RemoteJob) obj).getName());

  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public String toString() {
    return "RemoteJob: " + name + ", " + url + ", " + description;
  }
}
