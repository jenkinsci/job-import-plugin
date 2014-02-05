package org.jenkins.ci.plugins.jobimport;

import hudson.model.TopLevelItem;

import java.io.IOException;
import java.io.InputStream;

import com.cloudbees.hudson.plugins.folder.Folder;

/**
 * @author mrcaraion
 *
 */
public class FolderContainer implements JobImportContainer {
	
	final private Folder folder;
	
	public FolderContainer(Folder folder) {
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
