/*
 * Copyright 2021 Quest Software Inc.
 * ALL RIGHTS RESERVED.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either expressor implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ci.with.toad.edge;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import javax.servlet.ServletException;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import hudson.FilePath;
import hudson.model.Action;
import hudson.model.DirectoryBrowserSupport;
import hudson.model.Run;
import jenkins.model.RunAction2;

/**
 * 
 * @author pchudani
 *
 */
public class ReportPublisherAction implements Action, RunAction2, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private transient Run<?, ?> build;
	private File compareReportsDir;

	public ReportPublisherAction(File compareReportsDir) {
		super();
		this.compareReportsDir = compareReportsDir;
	}

	public void onAttached(Run<?, ?> arg0) {
		this.build = arg0;
	}

	public void onLoad(Run<?, ?> arg0) {
		this.build = arg0;
	}

	public String getDisplayName() {
		return "Compare Html report";
	}

	public String getIconFileName() {
		return "graph.gif";
		// return "/images/jenkins.png";
	}

	public String getUrlName() {
		return "comparehtmlreport";
	}

	/**
	 * Serves Toad reports.
	 * 
	 * @param req Request
	 * @param rsp Response
	 * 
	 * @throws IOException IOException
	 * @throws ServletException ServletException 
	 */
	public void doDynamic(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
		if (this.build != null) {
			DirectoryBrowserSupport dbs = new DirectoryBrowserSupport(this, new FilePath(this.compareReportsDir),
					"Compare Html Report", "graph.gif", false);
			dbs.setIndexFileName("index.html");
			dbs.generateResponse(req, rsp, this);
		}
	}
}
