package org.jenkins.ci.plugins.jobimport.model;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.security.ACL;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.util.Collections;

public class JenkinsSite extends AbstractDescribableImpl<JenkinsSite> {

    private final String name;
    private final String url;
    private String defaultCredentialsId;

    @DataBoundConstructor
    public JenkinsSite(String name, String url) {
        this.name = name;
        this.url = url;
    }

    @DataBoundSetter
    public void setDefaultCredentialsId(String defaultCredentialsId) {
        this.defaultCredentialsId = defaultCredentialsId;
    }

    public String getDefaultCredentialsId() {
        return defaultCredentialsId;
    }

    public String getUrl() {
        return url;
    }

    public String getName() {
        return name;
    }

    @Extension
    public static final class DescriptorImpl extends Descriptor<JenkinsSite> {
        @Override
        public String getDisplayName() {
            return "";
        }
        public ListBoxModel doFillDefaultCredentialsIdItems() {
            return new StandardListBoxModel()
                    .includeEmptyValue()
                    .includeMatchingAs(
                            Jenkins.getAuthentication(),
                            Jenkins.getInstance(),
                            StandardUsernamePasswordCredentials.class,
                            Collections.<DomainRequirement>emptyList(),
                            CredentialsMatchers.always()
                    ).includeMatchingAs(
                            ACL.SYSTEM,
                            Jenkins.getInstance(),
                            StandardUsernamePasswordCredentials.class,
                            Collections.<DomainRequirement>emptyList(),
                            CredentialsMatchers.always()
                    );
        }

    }

}
