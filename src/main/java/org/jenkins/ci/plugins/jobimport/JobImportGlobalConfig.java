package org.jenkins.ci.plugins.jobimport;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import hudson.Extension;
import hudson.security.ACL;
import hudson.util.ListBoxModel;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.jenkins.ci.plugins.jobimport.model.JenkinsSite;
import org.kohsuke.stapler.StaplerRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Extension
public class JobImportGlobalConfig extends GlobalConfiguration {

    private List<JenkinsSite> sites = new ArrayList<>();

    public JobImportGlobalConfig() {
        load();
    }

    public static JobImportGlobalConfig get() {
        return GlobalConfiguration.all().get(JobImportGlobalConfig.class);
    }

    @Override
    public boolean configure(final StaplerRequest req, final JSONObject formData) {
        setSites(req.bindJSONToList(JenkinsSite.class, formData.get("sites")));
        return false;
    }

    public List<JenkinsSite> getSites() {
        return sites;
    }

    public void setSites(final List<JenkinsSite> sites) {
        this.sites = sites;
        save();
    }

}
