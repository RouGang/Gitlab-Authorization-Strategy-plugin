package com.hunantv.jenkins.plugins.gitlabjenkins;

import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.userdetails.UserDetails;

import com.hunantv.gitlab.api.models.GitlabSession;
import com.hunantv.gitlab.api.models.GitlabUser;

/**
 * <p>Class Name: GitlabUserDetails.</p>
 * <p>Description: 类功能说明:用户信息</p>
 * <p>Sample: 该类的典型使用方法和用例</p>
 * <p>Author: hrg</p>
 * <p>Date: 2014年6月29日</p>
 * <p>Modified History: 修改记录，格式(Name)  (Version)  (Date) (Reason & Contents)</p>
 */
public class GitlabUserDetails implements UserDetails {
	
	private final GitlabSession session;
	
	public GitlabUserDetails(GitlabSession user){
		this.session=user;
	}

	@Override
	public GrantedAuthority[] getAuthorities() {
		return new GrantedAuthority [] {};
	}

	@Override
	public String getPassword() {
		return null;
	}

	@Override
	public String getUsername() {
		return this.session.getName();
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	public GitlabSession getSession() {
		return session;
	}
	

	
}
