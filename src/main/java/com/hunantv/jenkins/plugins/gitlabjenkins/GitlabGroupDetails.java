package com.hunantv.jenkins.plugins.gitlabjenkins;

import com.hunantv.gitlab.api.models.GitlabGroup;

import hudson.security.GroupDetails;

/**
 * <p>Class Name: GitlabGroupDetails.</p>
 * <p>Description: 类功能说明：组信息</p>
 * <p>Sample: 该类的典型使用方法和用例</p>
 * <p>Author: hrg</p>
 * <p>Date: 2014年6月29日</p>
 * <p>Modified History: 修改记录，格式(Name)  (Version)  (Date) (Reason & Contents)</p>
 */
public class GitlabGroupDetails extends GroupDetails {
	
	private final GitlabGroup group;
	
	public GitlabGroupDetails(GitlabGroup group){
		super();
		this.group=group;
		
	}
	
	@Override
	public String getName() {
		if(this.group!=null){
			return this.group.getName();
		}
		return null;
	}

}
