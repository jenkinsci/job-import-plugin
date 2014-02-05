package org.jenkins.ci.plugins.jobimport;

import java.io.IOException;
import java.io.InputStream;

import hudson.model.TopLevelItem;

/**
 * @author mrcaraion
 *
 */
public interface JobImportContainer {

	boolean hasJob(String name);
	
	TopLevelItem getJob(String name);
	
	TopLevelItem createProjectFromXML(String name, InputStream xml) throws IOException;
	
	String getUrl();
	
}
