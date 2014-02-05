package org.jenkins.ci.plugins.jobimport;

import hudson.Extension;
import hudson.model.RootAction;

/**
 * @author mrcaraion
 *
 */
@Extension
public final class JobRootImportAction extends JobImportAction implements RootAction {

	public JobRootImportAction() {
		super(new HudsonContainer());
	}

}
