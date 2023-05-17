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
		arguments.add("--add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED -jar");
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
