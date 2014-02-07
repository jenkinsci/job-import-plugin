/*
 * The MIT License
 * 
 * Copyright (c) 2013, Vivat Inc.
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

import hudson.model.TopLevelItem;

import java.io.IOException;
import java.io.InputStream;

import com.cloudbees.hudson.plugins.folder.Folder;

/**
 * @author mrcaraion
 *
 */
public class JobImportFolder implements JobImportContainer {
	
	final private Folder folder;
	
	public JobImportFolder(Folder folder) {
		this.folder = folder;
	}
	
	/* (non-Javadoc)
	 * @see org.jenkins.ci.plugins.jobimport.JobImportContainer#hasJob(java.lang.String)
	 */
	public boolean hasJob(String name) {
		return folder.getItem(name) != null;
	}
	
	/* (non-Javadoc)
	 * @see org.jenkins.ci.plugins.jobimport.JobImportContainer#getJob(java.lang.String)
	 */
	public TopLevelItem getJob(String name) {
		return folder.getItem(name);
	}

	/* (non-Javadoc)
	 * @see org.jenkins.ci.plugins.jobimport.JobImportContainer#createProjectFromXML(java.lang.String, java.io.InputStream)
	 */
	public TopLevelItem createProjectFromXML(String name, InputStream xml) 
			throws IOException {
		return folder.createProjectFromXML(name, xml);
	}

	/* (non-Javadoc)
	 * @see org.jenkins.ci.plugins.jobimport.JobImportContainer#getUrl()
	 */
	public String getUrl() {
		return folder.getAbsoluteUrl();
	}

}
