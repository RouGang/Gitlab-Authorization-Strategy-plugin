package com.hunantv.jenkins.plugins.gitlabjenkins;

import hudson.Extension;
import hudson.Util;
import hudson.model.Descriptor;
import hudson.security.AbstractPasswordBasedSecurityRealm;
import hudson.security.GroupDetails;
import hudson.security.UserMayOrMayNotExistException;
import hudson.security.SecurityRealm;

import java.io.IOException;
import java.util.logging.Logger;

import org.acegisecurity.AuthenticationException;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.userdetails.UserDetails;
import org.acegisecurity.userdetails.UserDetailsService;
import org.acegisecurity.userdetails.UsernameNotFoundException;
import org.kohsuke.stapler.DataBoundConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;

import com.hunantv.gitlab.api.models.GitlabGroup;
import com.hunantv.gitlab.api.models.GitlabSession;

public class GitLabSecurityRealm extends AbstractPasswordBasedSecurityRealm {
	
	/**
	 * gitlab api invoke url.
	 */
	private String DEFAULT_WEB_URI="http://git.hunantv.com/";
	
	private String gitlabWebUri;
	
	@DataBoundConstructor
	public GitLabSecurityRealm(String gitlabWebUri) {
		super();
		this.gitlabWebUri = Util.fixEmptyAndTrim(gitlabWebUri);
	
	}
	
	private GitLabSecurityRealm(){
	}
	
	
	

	public String getGitlabWebUri() {
		return gitlabWebUri;
	}

	public void setGitlabWebUri(String gitlabWebUri) {
		this.gitlabWebUri = gitlabWebUri;
	}

	
	
	@Override
	public SecurityComponents createSecurityComponents() {
		return new SecurityComponents(
			new GitLabAthenticationManager(this.gitlabWebUri),
			new UserDetailsService() {
				public UserDetails loadUserByUsername(String username)
						throws UserMayOrMayNotExistException, DataAccessException {
					throw new UserMayOrMayNotExistException("Cannot verify users in this context");
				}
			});
	}



	/**
	 *
	 * @param groupName
	 * @return
	 * @throws UsernameNotFoundException
	 * @throws DataAccessException
	 */
	@Override
	public GroupDetails loadGroupByGroupname(String groupName)
			throws UsernameNotFoundException, DataAccessException {

		GitLabAuthenticationToken authToken =  (GitLabAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

		if(authToken == null)
			throw new UsernameNotFoundException("No known group: " + groupName);

		try {
			GitlabGroup org = authToken.loadGroup(groupName);
			if (org != null)
				return new GitlabGroupDetails(org);
			else
				throw new UsernameNotFoundException("No known group: " + groupName);
		} catch (IOException e) {
			throw new DataRetrievalFailureException("loadGroupByGroupname (groupname=" + groupName +")", e);
		}
	}

	@Override
	public boolean allowsSignup() {
		return false;
	}
	
	
	
	@Override
	protected UserDetails authenticate(String username, String password)
			throws AuthenticationException {
		
		return loadUserByUsername(username);
	}
	
	@Override
	public UserDetails loadUserByUsername(String username)
			throws UsernameNotFoundException, DataAccessException {
		GitlabSession session = null;
		GitLabAuthenticationToken authToken =  (GitLabAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

		if (authToken == null) {
			throw new UserMayOrMayNotExistException("Could not get auth token.");
		}

		try {

		/*	GroupDetails group = loadGroupByGroupname(username);

			if (group != null) {
				throw new UsernameNotFoundException ("user("+username+") is also an group");
			}*/

			session = authToken.loadUser(username);

			if (session != null)
				return new GitlabUserDetails(session);
			else
				throw new UsernameNotFoundException("No known user: " + username);
		} catch (IOException e) {
			throw new DataRetrievalFailureException("loadUserByUsername (username=" + username +")", e);
		}
	}
	
	
	
	


	/**
	 * Logger for debugging purposes.
	 */
	private static final Logger LOGGER = Logger
			.getLogger(GitLabSecurityRealm.class.getName());

	@Extension
    public static final class DescriptorImpl extends Descriptor<SecurityRealm> {

        @Override
        public String getHelpFile() {
            return "/plugin/github-oauth/help/help-security-realm.html";
        }

        @Override
        public String getDisplayName() {
            return "Gitlab Authentication Plugin";
        }

        public DescriptorImpl() {
            super();
        }

        public DescriptorImpl(Class<? extends SecurityRealm> clazz) {
            super(clazz);
        }

    }

}
