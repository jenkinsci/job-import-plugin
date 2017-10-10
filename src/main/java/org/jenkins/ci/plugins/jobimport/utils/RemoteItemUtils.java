package org.jenkins.ci.plugins.jobimport.utils;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import org.jenkins.ci.plugins.jobimport.model.RemoteFolder;
import org.jenkins.ci.plugins.jobimport.model.RemoteItem;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

public final class RemoteItemUtils {

    public static String fullName(RemoteItem item) {
        if (item == null) {
            return "";
        }

        if (!item.hasParent()) {
            return item.getName();
        }

        StringBuilder sb = new StringBuilder();
        sb.insert(0,item.getName());

        RemoteFolder parent = item.getParent();
        while (parent != null) {
            sb.insert(0,Constants.SEPARATOR).insert(0, parent.getName());
            parent = parent.getParent();
        }

        return sb.toString();
    }

    private static String fullName(RemoteItem item, String name) {
        final String full = item.getName() + Constants.JOBS_SEPARATOR_F + name;
        return item.hasParent() ? fullName(item.getParent(), name) : name;
    }


    public static String text(Element e, String name) {
        NodeList nl = e.getElementsByTagName(name);
        if (nl.getLength() == 1) {
            Element e2 = (Element) nl.item(0);
            return e2.getTextContent();
        } else {
            return null;
        }
    }

    public static String cleanRemoteString(final String string) {
        return StringUtils.substring(StringEscapeUtils.escapeHtml(string), 0, Constants.MAX_STR_LEN);
    }


    public static RemoteItem getRemoteJob(SortedSet<RemoteItem> items, String jobUrl) {
        return findFirstMAtchingRemoteItem(items, jobUrl);
    }

    static RemoteItem findFirstMAtchingRemoteItem(SortedSet<RemoteItem> items, String filter) {
        final List<RemoteItem> list = new ArrayList<>();
        if (StringUtils.isNotEmpty(filter)) {
            for (RemoteItem item: items) {
                if (filter.trim().equals(item.getUrl().trim())) {
                    return item;
                }
            }
        }
        return null;
    }


    static List<RemoteItem> findRemoteItemAndDescendants(SortedSet<RemoteItem> items, String filter) {
        final List<RemoteItem> list = new ArrayList<>();
        if (StringUtils.isNotEmpty(filter)) {
            for (RemoteItem item: items) {
                if (filter.trim().equals(item.getUrl().trim())) {
                    list.add(item);
                    if(item.isFolder()) {
                        list.addAll(populateAllChildren((RemoteFolder)item));
                    }
                    break;
                }
            }
        }
        return list;
    }

    private static List<RemoteItem> populateAllChildren(RemoteFolder folder) {
        List<RemoteItem> list = new ArrayList<>();
        for (RemoteItem item : folder.getChildren()) {
            list.add(item);
            if (item.isFolder()) {
                list.addAll(populateAllChildren((RemoteFolder)item));
            }
        }
        return list;
    }
}
