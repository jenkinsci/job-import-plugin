/*
 * The MIT License
 *
 * Copyright (c) 2011, Manufacture Francaise des Pneumatiques Michelin,
 * Daniel Petisme, Romain Seguy
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
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.jenkins.ci.plugins.jobimport;

import hudson.Plugin;
import hudson.XmlFile;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import net.sf.json.JSONObject;
import org.jenkins.ci.plugins.jobimport.model.JenkinsInstance;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Displays the config pane in the global configuration UI.
 * 
 * @author Daniel Petisme <daniel.petisme@gmail.com> <http://danielpetisme.blogspot.com/>
 */
public class JobImportPlugin extends Plugin {

    private static final String CONFIG_FILE = "job-import-plugin.xml";
    /** Controls the display of the imported jobs description (default is true). */
    private boolean enableJobDescription = true;
    /** Controls the display of the "Rename" and "Pattern based replacement" fields. */
    private boolean enableAdvancedOptions;
    /**
     * By default, when they want to import some jobs, users have to specify the
     * URL of the Jenkins instance to import from; Enabling this option allows
     * defining (by the admin) a static list of Jenkins instances.
     */
    private boolean enableJenkinsList;
    /** Contains the pre-defined Jenkins instances. */
    private List<JenkinsInstance> jenkinsInstances = new ArrayList<JenkinsInstance>();

    public JobImportPlugin() {
    }

    @DataBoundConstructor
    public JobImportPlugin(String jobFilterPattern, boolean enableJenkinsList, boolean enableJobDescription, boolean enableAdvancedOptions, List<JenkinsInstance> jenkinsInstances) {
        this.enableJenkinsList = enableJenkinsList;
        this.enableJobDescription = enableJobDescription;
        this.enableAdvancedOptions = enableAdvancedOptions;
        this.jenkinsInstances = jenkinsInstances;
    }

    @Override
    public void configure(StaplerRequest req, JSONObject formData) throws IOException, ServletException, Descriptor.FormException {
        jenkinsInstances.clear();

        req.bindJSON(this, formData);

        save();
    }

    public boolean isEnableJobDescription() {
        return enableJobDescription;
    }

    public boolean isEnableAdvancedOptions() {
        return enableAdvancedOptions;
    }

    public boolean isEnableJenkinsList() {
        return enableJenkinsList;
    }

    public List<JenkinsInstance> getJenkinsInstances() {
        return jenkinsInstances;
    }

    @Override
    protected XmlFile getConfigXml() {
        return new XmlFile(Hudson.XSTREAM, new File(Hudson.getInstance().getRootDir(), CONFIG_FILE));
    }

    @Override
    public void start() throws Exception {
        super.start();

        // backward compatibility
        Hudson.XSTREAM.alias("JobImportPlugin", JobImportPlugin.class);
        Hudson.XSTREAM.aliasField("enableJenkinsList", JobImportPlugin.class, "enableJenkinsList");
        Hudson.XSTREAM.aliasField("enableJobDescription", JobImportPlugin.class, "enableJobDescription");
        Hudson.XSTREAM.aliasField("jenkinsInstances", JobImportPlugin.class, "jenkinsInstances");
        Hudson.XSTREAM.alias("JenkinsInstance", JenkinsInstance.class);
        Hudson.XSTREAM.aliasField("url", JenkinsInstance.class, "url");
        Hudson.XSTREAM.aliasField("login", JenkinsInstance.class, "login");
        Hudson.XSTREAM.aliasField("password", JenkinsInstance.class, "password");
        Hudson.XSTREAM.aliasField("jobFilter", JenkinsInstance.class, "jobFilter");

        load();
    }

}
