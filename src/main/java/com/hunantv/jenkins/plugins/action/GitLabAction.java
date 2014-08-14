package com.hunantv.jenkins.plugins.action;

import hudson.Extension;
import hudson.model.UnprotectedRootAction;

import java.io.IOException;

import javax.servlet.ServletException;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import com.hunantv.jenkins.plugins.response.GitLabResponse;

@Extension
public class GitLabAction implements UnprotectedRootAction {
	
	@Override
	public String getIconFileName() {
		return null;
	}

	@Override
	public String getDisplayName() {
		return null;
	}

	@Override
	public String getUrlName() {
		return "gitlab";
	}
	
	/**
     * Serves the badge image.
     */
    public HttpResponse doIcon(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        JSONObject obj=new JSONObject();
        obj.put("success", "true");
    	return new  GitLabResponse(obj.toString());
    }
}
