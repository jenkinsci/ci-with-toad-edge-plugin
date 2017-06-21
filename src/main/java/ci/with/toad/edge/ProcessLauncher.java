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
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Launcher.ProcStarter;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.util.ArgumentListBuilder;
import jenkins.model.GlobalConfiguration;

public class ProcessLauncher {

	/**
	 * Execute some command using library.
	 * 
	 * @param args
	 *            - map of command line arguments
	 * @param build
	 *            - build run
	 * @param launcher
	 *            - process starting launcher
	 * @param listener
	 *            - build listener
	 * @return process output value
	 * @throws IOException
	 *             - if IO exception occurred during execution
	 * @throws InterruptedException
	 *             - if execution of process was interrupted
	 */
	public static int exec(Map<String, String> args, AbstractBuild<?, ?> build, Launcher launcher,
			BuildListener listener) throws IOException, InterruptedException {
		EnvVars env = build.getEnvironment(listener);
		String javaHome = env.get("JAVA_HOME", System.getProperty("java.home"));
		
		String javaBin = javaHome + "/bin/java";
		FilePath cliFolder = getWorkspaceCliFolder(build);
		FilePath osgiDir = getOsgiDir(build);
		
		String exec = null;
		try {
			exec = cliFolder + "/" +  getCliJarName(build);
		} catch (Exception e) {
			listener.getLogger().println(e.getMessage());
		}
		
		ArgumentListBuilder arguments = new ArgumentListBuilder();
		arguments.add(javaBin);
		arguments.add("-jar");
		arguments.add(exec);
		arguments.add("-lib");
		arguments.add(cliFolder + "/lib");
		arguments.add("-workspace");
		arguments.add(osgiDir.toURI().getPath());
		for (Entry<String, String> e : args.entrySet()) {
			arguments.add(e.getKey());
			arguments.add(e.getValue());
		}

		if (!launcher.isUnix()) {
			arguments = arguments.toWindowsCommand();
		}

		ProcStarter procStarter = launcher.launch();

		int result = procStarter.stdout(listener.getLogger()).stderr(listener.getLogger()).cmds(arguments)
				.pwd(build.getModuleRoot()).join();

		cliFolder.deleteRecursive();
		return result;
	}
	
	private static String getCliJarName(AbstractBuild<?, ?> build) throws Exception {
		List<FilePath> list = new FilePath(build.getWorkspace(), "cli-dir").list();
		
		for (FilePath item : list) {
			if (item.getName().endsWith(".jar")) {
				return item.getName();
			}
		}
		
		throw new Exception("Configured folder doesn't contain CLI tools");
	}

	private static FilePath getOsgiDir(AbstractBuild<?, ?> build) throws IOException, InterruptedException {
		FilePath dir = new FilePath(build.getWorkspace(), "workspace");
		if (!dir.exists()) {
			dir.mkdirs();
		}
		return dir;
	}

	private static FilePath getWorkspaceCliFolder(AbstractBuild<?, ?> build) throws IOException, InterruptedException {
		MainConfiguration config = GlobalConfiguration.all().get(MainConfiguration.class);
		
		if (config == null) {
			throw new RuntimeException("MainConfiguration extension is not available.");
		}
		if (config.getLibs() == null) {
			throw new RuntimeException("Path to libraries folder is undefined.");
		}
		FilePath zippedLibs = new FilePath(new File(config.getLibs()));
		FilePath target = new FilePath(build.getWorkspace(), "cli-dir");
		
		zippedLibs.unzip(target);

		return target;
	}
}
