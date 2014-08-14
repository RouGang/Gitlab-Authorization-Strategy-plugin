package com.hunantv.jenkins.plugins.gitlabjenkins;

import org.acegisecurity.AuthenticationException;

/**
 * <p>Class Name: GitLabAuthenticationException.</p>
 * <p>Description: 类功能说明:鉴权异常</p>
 * <p>Sample: 该类的典型使用方法和用例</p>
 * <p>Author: hrg</p>
 * <p>Date: 2014年6月29日</p>
 * <p>Modified History: 修改记录，格式(Name)  (Version)  (Date) (Reason & Contents)</p>
 */
public class GitLabAuthenticationException extends AuthenticationException {
	
    public GitLabAuthenticationException(String msg, Throwable t)
    {
        super(msg, t);
    }

   
    public GitLabAuthenticationException(String msg)
    {
        super(msg);
    }
}
