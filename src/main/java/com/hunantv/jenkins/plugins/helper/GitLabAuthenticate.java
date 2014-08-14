package com.hunantv.jenkins.plugins.helper;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.acegisecurity.AuthenticationException;

import com.hunantv.gitlab.api.GitlabAPI;
import com.hunantv.gitlab.api.models.GitlabCommit;
import com.hunantv.gitlab.api.models.GitlabMergeRequest;
import com.hunantv.gitlab.api.models.GitlabNote;
import com.hunantv.gitlab.api.models.GitlabProject;
import com.hunantv.gitlab.api.models.GitlabSession;
import com.hunantv.jenkins.plugins.gitlabjenkins.GitLabAuthenticationException;
import com.hunantv.jenkins.plugins.gitlabjenkins.GitlabUserDetails;

/**
 * description: gitlab 鉴权
 * @author Administrator
 *
 */
public class GitLabAuthenticate {
	
	private static final Logger LOGGER = Logger.getLogger(GitLabAuthenticate.class.getName());
	/**
	 * description:返回gitlab用户信息
	 * @param hostUrl gitlab 地址
	 * @param userName 用户名
	 * @param password 密码
	 * @throws AuthenticationException
	 */
	public static GitlabUserDetails getGitLabUser(String hostUrl,String userName,String password)throws GitLabAuthenticationException{
		GitlabUserDetails details=null;
		try{
			GitlabSession session=GitlabAPI.connect(hostUrl, userName, password);
			details=new GitlabUserDetails(session);
            //创建用户.
		}catch(Exception e){
			LOGGER.log(Level.WARNING, e.getMessage());
			throw new GitLabAuthenticationException("gitlab authentication fail",e);
		}
		return details;
		
	}
	
	public static void createGitLabMergeRequestComments(){
		try {
			
			GitlabUserDetails details=getGitLabUser("http://git.hunantv.com", "hrg", "huanggang");
			GitlabAPI gitLabApi=GitlabAPI.connect("http://git.hunantv.com", details.getSession().getPrivateToken());
			GitlabMergeRequest gitRequest=new GitlabMergeRequest();
			gitRequest.setId(519);
			gitRequest.setProjectId(158);
			GitlabNote note=gitLabApi.createNote(gitRequest, "close test gitlab!");
			//关闭合并请求
			gitRequest.setState("reopen");
			gitLabApi.closeMergeRequestState(gitRequest);
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, e.getMessage());
		}
	}
	
	public static GitlabProject getProject(int projectId){
	    GitlabUserDetails details=getGitLabUser("http://git.hunantv.com", "hrg", "huanggang");
        GitlabAPI gitLabApi=GitlabAPI.connect("http://git.hunantv.com", details.getSession().getPrivateToken());
        try {
            GitlabProject project= gitLabApi.getProject(projectId);
            return project;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
        
	}
	
	
	public static GitlabCommit getCommits(int projectId){
        try {
            
            GitlabUserDetails details=getGitLabUser("http://git.hunantv.com", "hrg", "huanggang");
            GitlabAPI gitLabApi=GitlabAPI.connect("http://git.hunantv.com", details.getSession().getPrivateToken());
            GitlabMergeRequest gitlabMergeRequest=new GitlabMergeRequest();
            gitlabMergeRequest.setSourceProjectId(projectId);
            gitlabMergeRequest.setSourceBranch("master");
            List<GitlabCommit> commits = gitLabApi.getCommits(gitlabMergeRequest);
            /*Collections.sort(commits, new Comparator<GitlabCommit>() {
                public int compare(GitlabCommit o1, GitlabCommit o2) {
                    return o2.getCreatedAt().compareTo(o1.getCreatedAt());
                }
            });*/

            for (GitlabCommit gitlabCommit : commits) {
                System.out.println(gitlabCommit);
            }
            return commits.get(0);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, e.getMessage());
            return null;
        }
    }
	
	public static void main(String[] args) {
	    int projectId=179;
	    GitlabProject project=getProject(projectId);
        System.out.println(project);
	    GitlabCommit commit=getCommits(projectId);
	    System.out.println(commit);
	  
	}
	
}
