package org.jenkins.ci.plugins.jobimport.model;

import java.io.Serializable;

public class RemoteJob extends RemoteItem implements Serializable {

    public RemoteJob(String name, String impl, String url, String description, RemoteFolder parent) {
        super(name, impl, url, description, parent);
    }

    @Override
    public boolean isFolder() {
        return false;
    }
}
