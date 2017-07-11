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
 * Various utility functions for working with localized Messages.
 * 
 * @author <a href="mailto:jieryn@gmail.com">Jesse Farinacci</a>
 * @since 1.0
 */
public final class MessagesUtils {
  public static String formatFailedDuplicateJobName() {
    return Messages.Job_Import_Plugin_Import_Failed_Duplicate();
  }

  public static String formatFailedException(final Exception e) {
    return formatFailedException(e.getMessage());
  }

  public static String formatFailedException(final String message) {
    return Messages.Job_Import_Plugin_Import_Failed_Exception(message);
  }

  public static String formatSuccess() {
    return Messages.Job_Import_Plugin_Import_Success();
  }
  public static String formatSuccessNotReloaded() {
    return Messages.Job_Import_Plugin_Import_Success_NotReloaded();
  }
  /**
   * Static-only access.
   */
  private MessagesUtils() {
    /* static-only access */
  }
}
