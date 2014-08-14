package com.hunantv.jenkins.plugins.gitlabjenkins;

import hudson.model.User;
import hudson.tasks.Mailer;

import java.io.IOException;

import org.acegisecurity.context.SecurityContextHolder;
import org.apache.commons.lang.StringUtils;

import com.hunantv.jenkins.plugins.helper.GitLabAuthenticate;

import jenkins.model.Jenkins;

public class CreateJob {
	

	public static void createJob(String jobName){
		Jenkins h = Jenkins.getInstance();
		try {
			//h.createProject(FreeStyleProject.class, "huanggang");
			h.createProjectFromXML(jobName, CreateJob.class.getResourceAsStream("/com/hunantv/jenkins/plugins/gitlabjenkins/JobConfig.xml"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public static void main(String[] args) {
		GitlabUserDetails userDetails = GitLabAuthenticate.getGitLabUser("git.hunantv.com", "test_account","huanggang@123");
		String accessToken=userDetails.getSession().getPrivateToken();
	}
}
