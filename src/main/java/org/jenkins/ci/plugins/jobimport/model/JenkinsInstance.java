/*
 * The MIT License
 *
 * Copyright (c) 2011, Manufacture Francaise des Pneumatiques Michelin, Daniel Petisme
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

package org.jenkins.ci.plugins.jobimport.model;

import hudson.util.Secret;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Represent a remote Jenkins instance which exposes a set of jobs to import
 * into the current instance.
 * 
 * @author Daniel Petisme <daniel.petisme@gmail.com> <http://danielpetisme.blogspot.com/>
 */
public class JenkinsInstance {

    private String login;
    private Secret password;
    private String url;
    /** Default filter applied when a user query a Jenkins instance. */
    private String jobFilter;

    @DataBoundConstructor
    public JenkinsInstance(String url, String login, String password, String jobFilter) {
        this.url = url;
        this.login = login;
        this.password = Secret.fromString(password);
        this.jobFilter = jobFilter;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return Secret.toString(password);
    }

    public String getUrl() {
        return url;
    }

    public String getJobFilter() {
        return jobFilter;
    }

}
