package com.hunantv.jenkins.plugins.gitlabjenkins;

import hudson.plugins.git.UserRemoteConfig;
import hudson.security.SecurityRealm;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.GrantedAuthorityImpl;
import org.acegisecurity.providers.AbstractAuthenticationToken;
import org.apache.commons.collections.CollectionUtils;

import com.hunantv.gitlab.api.GitlabAPI;
import com.hunantv.gitlab.api.models.GitlabGroup;
import com.hunantv.gitlab.api.models.GitlabProject;
import com.hunantv.gitlab.api.models.GitlabSession;
import com.hunantv.gitlab.api.models.GitlabUser;

/**
 * <p>Class Name: GitLabAuthenticationToken.</p>
 * <p>Description: 类功能说明</p>
 * <p>Sample: 该类的典型使用方法和用例</p>
 * <p>Author: hrg</p>
 * <p>Date: 2014年6月29日</p>
 * <p>Modified History: 修改记录，格式(Name)  (Version)  (Date) (Reason & Contents)</p>
 */
public class GitLabAuthenticationToken extends AbstractAuthenticationToken {
	
	private  String accessToken;
	private  String userName;
	private boolean admin;
	private static final Logger LOGGER = Logger
            .getLogger(GitLabAuthenticationToken.class.getName());
	private  GitlabAPI gl; 

	 private final List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
	
	public GitLabAuthenticationToken(String accessToken, String gitLabWebUri) throws IOException {
		super(new GrantedAuthority[] {});
		this.accessToken = accessToken;
        this.gl = GitlabAPI.connect(gitLabWebUri, accessToken);
        GitlabUser me=this.gl.getCurrentSession();
        assert me!=null;
        setAuthenticated(true);
        this.userName = me.getUsername();
        authorities.add(SecurityRealm.AUTHENTICATED_AUTHORITY);
        for (GitlabGroup group : this.gl.getGroups()){
            authorities.add(new GrantedAuthorityImpl(group.getName()));
		}
   }
	
	public boolean hasGroupPermission(
            String groupName) {

        try {
            List<GitlabGroup> groups=this.gl.getGroups();
            if(CollectionUtils.isNotEmpty(groups)){
                for (GitlabGroup gitlabGroup : groups) {
                    if(gitlabGroup.getName().equals(groupName)){
                        return true;
                    }
                }
            }
            return false;
        } catch (Exception e) {
            throw new RuntimeException("authorization failed for user = ",e);
        }
    }

	
	
	public GitlabSession loadUser(String userName) throws IOException {
		if (this.gl != null && isAuthenticated()){
			GitlabSession user=this.gl.getCurrentSession();
			if(user.getName().equals(userName)){
				return user;
			}
		}
		
			return null;
	}
	
	
	public GitlabGroup loadGroup(String groupName)
			throws IOException {
		if (gl != null && isAuthenticated()){
			List<GitlabGroup> groups=this.gl.getGroups();
			if(CollectionUtils.isNotEmpty(groups)){
				for (GitlabGroup gitlabGroup : groups) {
					if(gitlabGroup.getName().equals(groupName)){
						return gitlabGroup;
					}
				}
			}
			
		}	
		
		return null;

	}
	
    public boolean isPublicRepository(final String repositoryName) {
        GitlabProject repository = loadRepository(repositoryName);
        if (repository == null) {
            // If we don't have access its either not there or private & hidden from us
            return false;
        } else {
            return repository.isPublic();
        }
    }
    
    
    public boolean hasRepositoryPermission(final String repositoryName) {

        try {
            List<GitlabProject> gitProject= this.gl.getProjects();
            if(CollectionUtils.isNotEmpty(gitProject)){
                for (GitlabProject gitlabProject : gitProject) {
                    if(gitlabProject.getName().equals(repositoryName)) {
                        return true;
                    }
                }
            }
           return false;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "an exception was thrown", e);
            throw new RuntimeException("authorization failed for user = "
                        + getName(), e);
        }
    }
    
    public boolean hasRepositoryPermission(List<UserRemoteConfig> list) {
        try {
            List<GitlabProject> gls= this.gl.getProjects();
            if(CollectionUtils.isNotEmpty(list)){
                for (UserRemoteConfig remoteConfig : list) {
					if(!containGitUrl(gls, remoteConfig.getUrl())){
						return false;
					}
				}
                return true;
            }
           return false;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "an exception was thrown", e);
            throw new RuntimeException("authorization failed for user = "
                        + getName(), e);
        }
    }
    
    private boolean containGitUrl(List<GitlabProject> list,String url){
    	if(CollectionUtils.isEmpty(list)){
    		return false;
    	}
    	
    	for (GitlabProject gitlabProject : list) {
            if(gitlabProject.getSshUrl().equals(url)||gitlabProject.getHttpUrl().equals(url)) {
                return true;
            }
        }
    	return false;
    	
    }
    
    
    public GitlabProject loadRepository(String repositoryName) {
        try {
            if (this.gl != null && isAuthenticated()) {
                List<GitlabProject> gitProject= this.gl.getProjects();
                if(CollectionUtils.isNotEmpty(gitProject)){
                    for (GitlabProject gitlabProject : gitProject) {
                        if(gitlabProject.getName().equals(repositoryName)) {
                            return gitlabProject;
                        }
                    }
                }
                return null;
               
            } else {
                return null;
            }
        } catch(FileNotFoundException e) {
            LOGGER.log(Level.WARNING, "Looks like a bad github URL OR the Jenkins user does not have access to the repository{0}", repositoryName);
            return null;
        } catch(IOException e) {
            LOGGER.log(Level.WARNING, "Looks like a bad github URL OR the Jenkins user does not have access to the repository{0}", repositoryName);
            return null;
        }
}
	
	@Override
    public GrantedAuthority[] getAuthorities() {
        return authorities.toArray(new GrantedAuthority[authorities.size()]);
    }
	
	@Override
	public Object getCredentials() {
		return ""; // do not expose the credential
	}

	@Override
	public Object getPrincipal() {
		return this.getDetails();
	}
	
	

	public boolean isAdmin() {
		return admin;
	}

	public void setAdmin(boolean admin) {
		this.admin = admin;
	}
	
	
	

}
