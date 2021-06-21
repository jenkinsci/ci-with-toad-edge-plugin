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
import java.io.Serializable;

import hudson.model.Action;

/**
 * 
 * @author pchudani
 *
 */
public class ReportFrameAction implements Action, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ReportFrameAction(File compareReportsDir) {
		super();
	}

	public String getDisplayName() {
		return null;
	}

	public String getIconFileName() {
		return null;
	}

	public String getUrlName() {
		return "reportframe";
	}
}
