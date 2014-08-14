package com.hunantv.jenkins.plugins.gitlabjenkins;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import hudson.model.Job;
import hudson.model.View;
import hudson.security.ACL;
import hudson.security.AuthorizationStrategy;
import hudson.security.Permission;

import java.util.ArrayList;
import java.util.Collection;

import org.acegisecurity.Authentication;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

public class GitlabAuthorizationStrategy extends AuthorizationStrategy {
    
    private final GitlabRequireOrganizationMembershipACL rootACL;
    
    @DataBoundConstructor
    public GitlabAuthorizationStrategy(String adminUserNames,
            boolean authenticatedUserReadPermission, boolean useRepositoryPermissions,
                        String groupNames,
            boolean allowGitLabWebHookPermission, boolean allowCcTrayPermission,
            boolean allowAnonymousReadPermission) {
        super();

        rootACL = new GitlabRequireOrganizationMembershipACL(adminUserNames,
        		groupNames, authenticatedUserReadPermission,
                                useRepositoryPermissions, allowGitLabWebHookPermission,
                                allowCcTrayPermission, allowAnonymousReadPermission);
    }

	@Override
	public ACL getRootACL() {
	    return rootACL;
	}
	
	@Override
	public ACL getACL(Job<?,?> job) {
        if(job instanceof AbstractProject) {
            AbstractProject project = (AbstractProject)job;
            GitlabRequireOrganizationMembershipACL gitlabACL = (GitlabRequireOrganizationMembershipACL) getRootACL();
            return gitlabACL.cloneForProject(project);
          } else {
            return getRootACL();
          }
    }
	
	@Override
	public ACL getACL(final View item) {
	        return new ACL() {
	            @Override
	            public boolean hasPermission(Authentication a, Permission permission) {
	            	if(a !=null && a instanceof GitLabAuthenticationToken){
	            		GitLabAuthenticationToken authenticationToken = (GitLabAuthenticationToken) a;
		            	if(authenticationToken.isAdmin()){
		            		return true;
		            	}
		                if(item instanceof GitLabMyView&& permission == View.READ){
		                	return true;
		                }
	            	}
	            	
	            	ACL base = item.getOwner().getACL();
	            	
	                boolean hasPermission = base.hasPermission(a, permission);
	                if (!hasPermission && permission == View.READ) {
	                    return base.hasPermission(a,View.CONFIGURE) || !item.getItems().isEmpty();
	                }

	                return hasPermission;
	            }
	        };
	 }

	@Override
	public Collection<String> getGroups() {
	    return new ArrayList<String>(0);
	}
	
	private Object readResolve() {
        return this;
    }

    /**
     * @return
     * @see org.jenkinsci.plugins.GithubRequireOrganizationMembershipACL#getOrganizationNameList()
     */
    public String getOrganizationNames() {
        return StringUtils.join(rootACL.getGroupNameList().iterator(), ", ");
    }

    /**
     * @return
     * @see org.jenkinsci.plugins.GithubRequireOrganizationMembershipACL#getAdminUserNameList()
     */
    public String getAdminUserNames() {
        return StringUtils.join(rootACL.getAdminUserNameList().iterator(), ", ");
    }

    /**
     * @return
     * @see org.jenkinsci.plugins.GithubRequireOrganizationMembershipACL#isUseRepositoryPermissions()
     */
    public boolean isUseRepositoryPermissions() {
        return rootACL.isUseRepositoryPermissions();
    }

    /**
     * @return
     * @see org.jenkinsci.plugins.GithubRequireOrganizationMembershipACL#isAuthenticatedUserReadPermission()
     */
    public boolean isAuthenticatedUserReadPermission() {
        return rootACL.isAuthenticatedUserReadPermission();
    }

    /**
     * @return
     * @see org.jenkinsci.plugins.GithubRequireOrganizationMembershipACL#isAllowGithubWebHookPermission()
     */
    public boolean isAllowGitLabWebHookPermission() {
        return rootACL.isAllowGitLabWebHookPermission();
    }

    /**
     * @return
     * @see org.jenkinsci.plugins.GithubRequireOrganizationMembershipACL#isAllowCcTrayPermission()
     */
    public boolean isAllowCcTrayPermission() {
        return rootACL.isAllowCcTrayPermission();
    }


    /**
     * @return
     * @see org.jenkinsci.plugins.GithubRequireOrganizationMembershipACL#isAllowAnonymousReadPermission()
     */
    public boolean isAllowAnonymousReadPermission() {
        return rootACL.isAllowAnonymousReadPermission();
    }


    @Extension
    public static final class DescriptorImpl extends
            Descriptor<AuthorizationStrategy> {

        public String getDisplayName() {
            return "Gitlab Commiter Authorization Strategy";
        }

        public String getHelpFile() {
            return "/plugin/gitlab-oauth/help/help-authorization-strategy.html";
        }
    }

}
