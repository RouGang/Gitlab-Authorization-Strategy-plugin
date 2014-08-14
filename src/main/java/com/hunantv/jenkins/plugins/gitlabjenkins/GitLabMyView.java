package com.hunantv.jenkins.plugins.gitlabjenkins;

import hudson.Extension;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.ModifiableItemGroup;
import hudson.model.TopLevelItem;
import hudson.model.ViewGroup;
import hudson.model.Descriptor.FormException;
import hudson.model.Job;
import hudson.model.Project;
import hudson.model.View;
import hudson.model.ViewDescriptor;
import hudson.plugins.git.GitSCM;
import hudson.scm.SCM;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import jenkins.model.Jenkins;

import org.acegisecurity.context.SecurityContextHolder;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;


public class GitLabMyView extends View {
	 private static final Logger log = Logger.getLogger(GitLabMyView.class.getName());

	 @DataBoundConstructor
	    public GitLabMyView(String name) {
	        super(name);
	    }

	    public GitLabMyView(String name, ViewGroup owner) {
	        this(name);
	        this.owner = owner;
	    }

	    @Override
	    public boolean contains(TopLevelItem item) {
	        return item.hasPermission(Job.CONFIGURE);
	    }

	    @Override
	    public TopLevelItem doCreateItem(StaplerRequest req, StaplerResponse rsp)
	            throws IOException, ServletException {
	        ItemGroup<? extends TopLevelItem> ig = getOwnerItemGroup();
	        if (ig instanceof ModifiableItemGroup) {
	            return ((ModifiableItemGroup<? extends TopLevelItem>)ig).doCreateItem(req, rsp);
	        }
	        return null;
	    }

	    @Override
	    public Collection<TopLevelItem> getItems() {
	        List<TopLevelItem> items = new ArrayList<TopLevelItem>();
	        for (TopLevelItem item : getOwnerItemGroup().getItems()) {
	        	GitLabAuthenticationToken authToken =  (GitLabAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
            	if(authToken.isAdmin()){
            		items.add(item);
            	}else{
            		if(item instanceof Project){
            			Project project=(Project)item;
            			SCM scm=project.getScm();
            			if(scm instanceof GitSCM ){
            				GitSCM gc=(GitSCM) scm;
            				if(authToken.hasRepositoryPermission(gc.getUserRemoteConfigs())){
            					items.add(item);
            				}
            			}else{
            				log.warning("please set project's git info!");
            			}
            			
            			
            		}
            	}
            	
            	
            	/*else if(authToken.hasRepositoryPermission(item.getName())){
	            	if(authToken)
            		items.add(item);
	            }*/
            	
	        	
	        	/*if (item.hasPermission(Job.CONFIGURE)) {
	                items.add(item);
	            }*/
	        }
	        return Collections.unmodifiableList(items);
	    }

	    @Override
	    public String getPostConstructLandingPage() {
	        return ""; // there's no configuration page
	    }

	    @Override
	    public void onJobRenamed(Item item, String oldName, String newName) {
	        // noop
	    }

	    @Override
	    protected void submit(StaplerRequest req) throws IOException, ServletException, FormException {
	        // noop
	    }

	    @Extension
	    public static final class DescriptorImpl extends ViewDescriptor {
	        /**
	         * If the security is not enabled, there's no point in having
	         * this type of views.
	         */
	        @Override
	        public boolean isInstantiable() {
	            return Jenkins.getInstance().isUseSecurity();
	        }

	        public String getDisplayName() {
	            return "gitalbViews";
	        }
	    }
	}


