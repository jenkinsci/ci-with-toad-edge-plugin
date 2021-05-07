/*
 * Continuous Integration with Toad Edge License Agreement
 * Version 2.0
 * Copyright 2021 Quest Software Inc.
 * ALL RIGHTS RESERVED.

 * Redistribution and use in source and binary forms, with or without modification, are permitted 
 * provided that the following conditions are met:
 *
 *	1) Redistributions of source code must retain the complete text of this license. 
 *
 *	2) Neither the name of Quest Software nor the names of subsequent contributors may be 
 *	used to endorse or promote products derived from this software without specific prior 
 *  written permission.  
 *
 *  3) Redistributions in binary form must reproduce the above copyright notice, this list of 
 *	conditions and the following disclaimer in the online product documentation and/or other
 *	materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY QUEST SOFTWARE INC. AND CONTRIBUTORS ``AS IS'' AND ANY 
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL 
 * THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS 
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
