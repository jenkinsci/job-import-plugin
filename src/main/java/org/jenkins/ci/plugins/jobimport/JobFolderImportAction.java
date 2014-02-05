package org.jenkins.ci.plugins.jobimport;

import hudson.Extension;
import hudson.model.Action;

import java.util.Collection;
import java.util.Collections;

import com.cloudbees.hudson.plugins.folder.TransientFolderActionFactory;
import com.cloudbees.hudson.plugins.folder.Folder;

/**
 * @author mrcaraion
 *
 */
@Extension
public class JobFolderImportAction extends TransientFolderActionFactory {

	/* (non-Javadoc)
	 * @see com.cloudbees.hudson.plugins.folder.TransientFolderActionFactory#createFor(com.cloudbees.hudson.plugins.folder.Folder)
	 */
	@Override
	public Collection<? extends Action> createFor(Folder target) {
		return Collections.singleton(new JobImportAction(new FolderContainer(target)));
	}

}
