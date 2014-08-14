package com.hunantv.jenkins.plugins.gitlabjenkins;

import hudson.model.User;
import hudson.tasks.Mailer;

import java.io.IOException;
import java.util.logging.Logger;

import org.acegisecurity.Authentication;
import org.acegisecurity.AuthenticationException;
import org.acegisecurity.AuthenticationManager;
import org.acegisecurity.context.SecurityContextHolder;
import org.apache.commons.lang.StringUtils;

import com.hunantv.jenkins.plugins.helper.GitLabAuthenticate;

public class GitLabAthenticationManager implements AuthenticationManager {
	private String gitlabWebUri;
	/**
	 * Logger for debugging purposes.
	 */
	private static final Logger LOGGER = Logger
			.getLogger(GitLabAthenticationManager.class.getName());

	public GitLabAthenticationManager(String gitlabWebUri) {
		this.gitlabWebUri = gitlabWebUri;
	}

	@Override
	public Authentication authenticate(Authentication authentication)
			throws AuthenticationException {
		GitlabUserDetails userDetails = GitLabAuthenticate.getGitLabUser(
				this.gitlabWebUri, (String) authentication.getName(),
				(String) authentication.getCredentials());
		String accessToken = userDetails.getSession().getPrivateToken();
		GitLabAuthenticationToken auth = null;
		if (StringUtils.isNotBlank(accessToken)) {
			// only set the access token if it exists.

			try {
				auth = new GitLabAuthenticationToken(accessToken,
						this.gitlabWebUri);
				auth.setDetails(userDetails);
				SecurityContextHolder.getContext().setAuthentication(auth);
			} catch (IOException e) {
				e.printStackTrace();
			}

			User u = User.current();
			u.setFullName(userDetails.getSession().getName());
			if (!u.getProperty(Mailer.UserProperty.class)
					.hasExplicitlyConfiguredAddress()) {
				try {
					u.addProperty(new Mailer.UserProperty(userDetails
							.getSession().getEmail()));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else {
			LOGGER.info("gitlab did not return an access token.");
		}

		return auth;
	}

}
