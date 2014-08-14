package com.hunantv.jenkins.plugins.response;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;

import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

public class GitLabResponse implements HttpResponse {
	
	private final String payload;

	/**
	 * To improve the caching, compute unique ETag.
	 * 
	 * This needs to differentiate different image types, and possible future
	 * image changes in newer versions of this plugin.
	 */


	
	public GitLabResponse(String json)  {
		payload=json;
	}

	@Override
	public void generateResponse(StaplerRequest req, StaplerResponse rsp,
			Object node) throws IOException, ServletException {
		rsp.setHeader("Cache-Control", "no-cache, private");
		rsp.setHeader("Content-Type", "application/json;charset=utf-8");
		ServletOutputStream outPut=rsp.getOutputStream();
		outPut.write(payload.getBytes());
		outPut.close();
	}

}
