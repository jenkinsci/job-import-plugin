package org.jenkins.ci.plugins.jobimport.client;

import org.apache.commons.lang.StringUtils;
import org.jenkins.ci.plugins.jobimport.model.RemoteFolder;
import org.jenkins.ci.plugins.jobimport.model.RemoteItem;
import org.jenkins.ci.plugins.jobimport.model.RemoteJob;
import org.jenkins.ci.plugins.jobimport.utils.Constants;
import org.jenkins.ci.plugins.jobimport.utils.CredentialsUtils;
import org.jenkins.ci.plugins.jobimport.utils.RemoteItemUtils;
import org.jenkins.ci.plugins.jobimport.utils.URLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class RestApiClient {

    private static final Logger LOG = Logger.getLogger(RestApiClient.class.getName());

    public static List<RemoteItem> getRemoteItems(RemoteFolder parent, String url, CredentialsUtils.NullSafeCredentials credentials, boolean recursiveSearch) {
        List<RemoteItem> items = new ArrayList<>();
        try {
            if (StringUtils.isNotEmpty(url)) {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
                factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
                factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
                factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
                Document doc = factory.newDocumentBuilder().parse(
                        URLUtils.fetchUrl(URLUtils.safeURL(url , Constants.XML_API_QUERY), credentials.username, credentials.password));
                NodeList nl = doc.getElementsByTagName("job");

                for (int i = 0; i < nl.getLength(); i++) {
                    Element job = (Element) nl.item(i);
                    String impl = job.getAttribute("_class");
                    boolean folder = (impl != null &&
                            "com.cloudbees.hudson.plugins.folder.Folder".equals(impl));
                    String desc = RemoteItemUtils.text(job, "description");
                    String jobUrl = url + "/job" + RemoteItemUtils.text(job, "url").split("job")[1];
                    String name = RemoteItemUtils.text(job, "name");

                    final RemoteItem item = folder ?
                            new RemoteFolder(name, impl, jobUrl, desc, parent) :
                            new RemoteJob(name, impl, jobUrl, desc, parent);
                    if(parent == null) {
                        items.add(item);
                    } else {
                        parent.getChildren().add(item);
                        items.add(item);
                    }

                    if(folder && recursiveSearch) {
                        items.addAll(getRemoteItems((RemoteFolder) item, jobUrl, credentials, true));
                    }

                }
            }
        } catch(Exception e) {
            LOG.log(Level.SEVERE, (new StringBuilder()).append("Failed to list job from remote ").append(url).toString(), e);
        }
        return items;
    }



}
