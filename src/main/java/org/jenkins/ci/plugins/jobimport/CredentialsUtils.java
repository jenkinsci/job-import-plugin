package org.jenkins.ci.plugins.jobimport;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import com.google.common.base.Strings;
import hudson.model.Item;
import hudson.security.ACL;

import java.util.Collections;
import java.util.List;

import static com.google.inject.internal.guava.base.$Preconditions.checkNotNull;

/**
 * Created by evildethow on 28/06/2016.
 */
final class CredentialsUtils {

  private CredentialsUtils() {
    throw new UnsupportedOperationException("Cannot instantiate utility class");
  }

  static NullSafeCredentials getCredentials(String credentialId) {
    if (!Strings.isNullOrEmpty(credentialId)) {
      StandardUsernamePasswordCredentials cred = CredentialsMatchers.firstOrNull(allCredentials(), CredentialsMatchers.withId(credentialId));
      if (cred != null) {
        return new NullSafeCredentials(cred.getUsername(), cred.getPassword().getPlainText());
      }
    }
    return new NullSafeCredentials();
  }

  static List<StandardUsernamePasswordCredentials> allCredentials() {
    return CredentialsProvider.lookupCredentials(
        StandardUsernamePasswordCredentials.class,
        (Item) null,
        ACL.SYSTEM,
        Collections.<DomainRequirement>emptyList()
    );
  }

  static final class NullSafeCredentials {

    final String username;
    final String password;

    NullSafeCredentials(String username, String password) {
      this.username = checkNotNull(username);
      this.password = checkNotNull(password);
    }

    NullSafeCredentials() {
      this.username = "";
      this.password = "";
    }
  }
}
