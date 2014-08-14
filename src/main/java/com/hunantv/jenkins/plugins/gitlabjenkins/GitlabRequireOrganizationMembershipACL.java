package com.hunantv.jenkins.plugins.gitlabjenkins;

import hudson.model.AbstractProject;
import hudson.plugins.git.GitSCM;
import hudson.plugins.git.UserRemoteConfig;
import hudson.scm.SCM;
import hudson.security.ACL;
import hudson.security.Permission;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import jenkins.model.Jenkins;

import org.acegisecurity.Authentication;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;

/**
 * <p>Class Name: GitlabRequireOrganizationMembershipACL.</p>
 * <p>Description: 类功能说明:访问开裂不</p>
 * <p>Sample: 该类的典型使用方法和用例</p>
 * <p>Author: hrg</p>
 * <p>Date: 2014年6月29日</p>
 * <p>Modified History: 修改记录，格式(Name)  (Version)  (Date) (Reason & Contents)</p>
 */
public class GitlabRequireOrganizationMembershipACL extends ACL {

    private static final Logger log = Logger.getLogger(GitlabRequireOrganizationMembershipACL.class.getName());

    private final List<String> groupNameList;
    private final List<String> adminUserNameList;
    private final boolean authenticatedUserReadPermission;
    private final boolean useRepositoryPermissions;
    private final boolean allowGitLabWebHookPermission;
    private final boolean allowCcTrayPermission;
    private final boolean allowAnonymousReadPermission;
    private final AbstractProject project;

    private String hudson_model_Hudson_Read = "hudson.model.Hudson.Read";
    private String hudson_model_Item_Workspace = "hudson.model.Item.Workspace";
    private String hudson_model_Item_Read = "hudson.model.Item.Read";

    @Override
    public boolean hasPermission(Authentication a, Permission permission) {
        //所有用户均可以访问到build项目之后的状态.
        if (permission != null && permission.getId().equals("hudson.model.Item.ViewStatus")) {
            return true;
        }
        
        if (a != null && a instanceof GitLabAuthenticationToken) {
            if (!a.isAuthenticated())
                return false;

            GitLabAuthenticationToken authenticationToken = (GitLabAuthenticationToken) a;
            String candidateName = a.getName();

            if (adminUserNameList.contains(candidateName)) {
                // if they are an admin then they have permission
                log.finest("Granting Admin rights to user " + candidateName);
                authenticationToken.setAdmin(true);
                return true;
            }

            if (this.project != null) {
                if (useRepositoryPermissions) {
                    if (hasRepositoryPermission(authenticationToken, permission)) {
                        log.finest("Granting Authenticated User " + permission.getId() + " permission on project " + project.getName() + "to user "
                                + candidateName);
                        return true;
                    }
                } else {
                    if (authenticatedUserReadPermission) {
                        if (checkReadPermission(permission)) {
                            log.finest("Granting Authenticated User read permission " + "on project " + project.getName() + "to user "
                                    + candidateName);
                            return true;
                        }
                    }
                }
            } else if (authenticatedUserReadPermission) {

                if (checkReadPermission(permission)) {

                    // if we support authenticated read and this is a read
                    // request we allow it
                    log.finest("Granting Authenticated User read permission to user " + candidateName);
                    return true;
                }
            }

            for (String groupName : this.groupNameList) {

                if (authenticationToken.hasGroupPermission(groupName)) {

                    String[] parts = permission.getId().split("\\.");

                    String test = parts[parts.length - 1].toLowerCase();

                    if (checkReadPermission(permission) || testBuildPermission(permission)) {
                        // check the permission

                        log.finest("Granting READ and BUILD rights to user " + candidateName + " a member of " + groupName);
                        return true;
                    }
                }

            }

            // no match.
            return false;

        } else {

            String authenticatedUserName = a.getName();

            if (authenticatedUserName.equals(SYSTEM.getPrincipal())) {
                // give system user full access
                log.finest("Granting Full rights to SYSTEM user.");
                return true;
            }

            if (authenticatedUserName.equals("anonymous")) {
                if (permission.getId().equals(hudson_model_Hudson_Read)) {
                    return true;
                }
                return false;
            }

            if (adminUserNameList.contains(authenticatedUserName)) {
                // if they are an admin then they have all permissions
                log.finest("Granting Admin rights to user " + a.getName());
                return true;
            }

            // else:
            // deny request
            //
            return false;

        }

    }

    public boolean hasRepositoryPermission(GitLabAuthenticationToken authenticationToken, Permission permission) {
        String repositoryName = getRepositoryName();

        if (checkReadPermission(permission)) {
            return true;
        } else {
            return authenticationToken.hasRepositoryPermission(repositoryName);
        }
    }

    private boolean currentUriPathEquals(String specificPath) {
        String requestUri = requestURI();
        if (requestUri != null) {
            String basePath = URI.create(Jenkins.getInstance().getRootUrl()).getPath();
            return URI.create(requestUri).getPath().equals(basePath + specificPath);
        } else {
            return false;
        }
    }

    private String requestURI() {
        StaplerRequest currentRequest = Stapler.getCurrentRequest();
        return (currentRequest == null) ? null : currentRequest.getOriginalRequestURI();
    }

    private boolean testBuildPermission(Permission permission) {
        if (permission.getId().equals("hudson.model.Hudson.Build") || permission.getId().equals("hudson.model.Item.Build")) {
            return true;
        } else
            return false;
    }

    private boolean checkReadPermission(Permission permission) {
        if (permission.getId().equals("hudson.model.Hudson.Read") || permission.getId().equals("hudson.model.Item.Workspace")
                || permission.getId().equals("hudson.model.Item.Read")) {
            return true;
        } else
            return false;
    }

    private String getRepositoryName() {
        String repositoryName = null;
        SCM scm = this.project.getScm();
        if (scm instanceof GitSCM) {
            GitSCM git = (GitSCM) scm;
            List<UserRemoteConfig> userRemoteConfigs = git.getUserRemoteConfigs();
            if (!userRemoteConfigs.isEmpty()) {
                String repoUrl = userRemoteConfigs.get(0).getUrl();
                if (repoUrl != null) {
                    GitLabRepositoryName githubRepositoryName = GitLabRepositoryName.create(repoUrl);
                    repositoryName = githubRepositoryName.userName + "/" + githubRepositoryName.repositoryName;
                }
            }
        }
        return repositoryName;
    }

    public GitlabRequireOrganizationMembershipACL(String adminUserNames, String organizationNames, boolean authenticatedUserReadPermission,
            boolean useRepositoryPermissions, boolean allowGitLabWebHookPermission, boolean allowCcTrayPermission,
            boolean allowAnonymousReadPermission) {
        super();
        this.authenticatedUserReadPermission = authenticatedUserReadPermission;
        this.useRepositoryPermissions = useRepositoryPermissions;
        this.allowGitLabWebHookPermission = allowGitLabWebHookPermission;
        this.allowCcTrayPermission = allowCcTrayPermission;
        this.allowAnonymousReadPermission = allowAnonymousReadPermission;

        this.adminUserNameList = new LinkedList<String>();

        String[] parts = adminUserNames.split(",");

        for (String part : parts) {
            adminUserNameList.add(part.trim());
        }

        this.groupNameList = new LinkedList<String>();

        parts = organizationNames.split(",");

        for (String part : parts) {
            groupNameList.add(part.trim());
        }

        this.project = null;

    }

    public GitlabRequireOrganizationMembershipACL(List<String> adminUserNameList, List<String> groupNameList,
            boolean authenticatedUserReadPermission, boolean useRepositoryPermissions, boolean allowGitLabWebHookPermission,
            boolean allowCcTrayPermission, boolean allowAnonymousReadPermission, AbstractProject project) {
        super();
        this.adminUserNameList = adminUserNameList;
        this.groupNameList = groupNameList;
        this.authenticatedUserReadPermission = authenticatedUserReadPermission;
        this.useRepositoryPermissions = useRepositoryPermissions;
        this.allowGitLabWebHookPermission = allowGitLabWebHookPermission;
        this.allowCcTrayPermission = allowCcTrayPermission;
        this.allowAnonymousReadPermission = allowAnonymousReadPermission;
        this.project = project;
    }

    public GitlabRequireOrganizationMembershipACL cloneForProject(AbstractProject project) {
        return new GitlabRequireOrganizationMembershipACL(this.adminUserNameList, this.groupNameList, this.authenticatedUserReadPermission,
                this.useRepositoryPermissions, this.allowGitLabWebHookPermission, this.allowCcTrayPermission, this.allowAnonymousReadPermission,
                project);
    }

    public List<String> getGroupNameList() {
        return groupNameList;
    }

    public List<String> getAdminUserNameList() {
        return adminUserNameList;
    }

    public boolean isUseRepositoryPermissions() {
        return useRepositoryPermissions;
    }

    public boolean isAuthenticatedUserReadPermission() {
        return authenticatedUserReadPermission;
    }

    public boolean isAllowGitLabWebHookPermission() {
        return allowGitLabWebHookPermission;
    }

    public boolean isAllowCcTrayPermission() {
        return allowCcTrayPermission;
    }

    /**
     * @return the allowAnonymousReadPermission
     */
    public boolean isAllowAnonymousReadPermission() {
        return allowAnonymousReadPermission;
    }

}
