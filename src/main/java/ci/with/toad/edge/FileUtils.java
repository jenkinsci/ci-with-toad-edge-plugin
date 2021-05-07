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

import java.nio.file.Paths;

import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.remoting.VirtualChannel;

public class FileUtils {

	public static final String WORKSPACE_VAR = "${WORKSPACE}";
	public static final String JOB_ROOT_DIR_VAR = "${JOB_ROOT_DIR}";
	
	public static FilePath getFilePath(AbstractBuild<?, ?> build, String path) {
		if (path.startsWith(WORKSPACE_VAR)) {
			return new FilePath(build.getWorkspace(), getRelativePath(path.substring(WORKSPACE_VAR.length())));
		}
		
		if (path.startsWith(JOB_ROOT_DIR_VAR)) {
			return new FilePath(new FilePath(build.getRootDir()), getRelativePath(path.substring(JOB_ROOT_DIR_VAR.length())));
		}
		
		if (Paths.get(path).isAbsolute()) {
			return new FilePath((VirtualChannel)null, path);	
		}
		return new FilePath(build.getWorkspace(), path);
	}
	
	private static String getRelativePath(String path) {
		if (path.startsWith("./")) {
			return path.substring(2);
		}
		if (path.startsWith(".\\")) {
			return path.substring(2);
		}
		if (path.startsWith("/")) {
			return path.substring(1);
		}
		if (path.startsWith("\\")) {
			return path.substring(1);
		}
		return path;
	}
}
