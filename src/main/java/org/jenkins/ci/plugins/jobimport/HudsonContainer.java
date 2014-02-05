package org.jenkins.ci.plugins.jobimport;

import hudson.model.TopLevelItem;
import hudson.model.Hudson;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author mrcaraion
 *
 */
public class HudsonContainer implements JobImportContainer {

	
	/* (non-Javadoc)
	 * @see org.jenkins.ci.plugins.jobimport.JobImportContainer#hasJob(java.lang.String)
	 */
	public boolean hasJob(String name) {
		return Hudson.getInstance().getItem(name) != null;
	}
	
	/* (non-Javadoc)
	 * @see org.jenkins.ci.plugins.jobimport.JobImportContainer#getJob(java.lang.String)
	 */
	public TopLevelItem getJob(String name) {
		return Hudson.getInstance().getItem(name);
	}

	/* (non-Javadoc)
	 * @see org.jenkins.ci.plugins.jobimport.JobImportContainer#createProjectFromXML(java.lang.String, java.io.InputStream)
	 */
	public TopLevelItem createProjectFromXML(String name, InputStream xml)
		throws IOException {
		return Hudson.getInstance().createProjectFromXML(name, xml);
	}

	/* (non-Javadoc)
	 * @see org.jenkins.ci.plugins.jobimport.JobImportContainer#getUrl()
	 */
	public String getUrl() {
		return Hudson.getInstance().getRootUrl();
	}

}
