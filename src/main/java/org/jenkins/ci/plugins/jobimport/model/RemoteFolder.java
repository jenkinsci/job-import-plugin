package org.jenkins.ci.plugins.jobimport.model;


import java.io.Serializable;
import java.util.SortedSet;
import java.util.TreeSet;

public class RemoteFolder extends RemoteItem implements Serializable {

    private final SortedSet<RemoteItem> children = new TreeSet<RemoteItem>();

    public RemoteFolder(String name, String impl, String url, String description, RemoteFolder parent) {
        super(name, impl, url, description, parent);
    }

    public SortedSet<RemoteItem> getChildren() {
        return children;
    }

    public boolean hasChildren() {
        return !children.isEmpty();
    }

    @Override
    public boolean isFolder() {
        return true;
    }
}
