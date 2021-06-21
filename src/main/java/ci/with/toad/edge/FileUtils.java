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
