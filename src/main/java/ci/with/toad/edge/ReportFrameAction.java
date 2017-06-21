/*
 * Continuous Integration with Toad Edge License Agreement
 * Version 1.0
 * Copyright 2017 Quest Software Inc.
 * ALL RIGHTS RESERVED.

 * Redistribution and use in source and binary forms, with or without modification, are permitted 
 * provided that the following conditions are met:
 *
 *	1) Redistributions of source code must retain the complete text of this license. 
 *	
 *	2) Redistributions in binary form must reproduce the above copyright notice, this list of 
 *	conditions and the following disclaimer in the online product documentation and/or other 
 *	materials provided with the distribution. 
 *
 *	3) Neither the name of Quest Software nor the names of subsequent contributors may be 
 *	used to endorse or promote products derived from this software without specific prior 
 *	written permission. 
 *
 *	4) THIS LICENSE DOES NOT INCLUDE A TRADEMARK LICENSE to use Quest Software 
 *	trademarks, including but not limited to Quest, Quest Software, Toad or Toad Edge.  You 
 * 	may use and modify this component, but your modifications must not include additional use 
 *	of Quest Software marks beyond the name of this component and its license.
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
