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
import java.util.HashMap;
import java.util.Map;

import org.jvnet.localizer.Localizable;
import org.jvnet.localizer.ResourceBundleHolder;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Run;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;

/**
 * Builder implementation used to define "Generate Report" build step.
 *
 * @author pchudani
 */
public class GenerateJenkinsReportBuilder extends Builder {

	private String OUTPUT = "JENKINS_REPORT";
	private String INPUT = "TMP_INPUT";
	private String inputFolder;

	// Fields in config.jelly must match the parameter names in the
	// "DataBoundConstructor"
	@DataBoundConstructor
	public GenerateJenkinsReportBuilder(String inputFolder) {
		this.inputFolder = inputFolder;
	}

	/**
	 * @return Input folder location. used from the <tt>config.jelly</tt> to
	 *         display on build step.
	 */
	public String getInputFolder() {
		return inputFolder;
	}

	protected File getCompareReportsDir(Run<?, ?> build) {
		return new File(build.getRootDir(), OUTPUT);
	}

	private FilePath getTmpIn(AbstractBuild<?, ?> build) {
		return new FilePath(build.getWorkspace(), INPUT + build.number);
	}

	private FilePath getTmpOut(AbstractBuild<?, ?> build) {
		return new FilePath(build.getWorkspace(), OUTPUT + build.number);
	}
	

	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
			throws IOException, InterruptedException {
		// This is where you 'build' the project.
		System.setOut(listener.getLogger());
		System.setErr(listener.getLogger());

		copyBuildFiles(build, listener);
		FilePath tmpOutput = getTmpOut(build);
		tmpOutput.mkdirs();

		Map<String, String> arguments = new HashMap<>();
		arguments.put("-out", tmpOutput.toURI().getPath());
		arguments.put("-in", getTmpIn(build).toURI().getPath());
		arguments.put("-report", "");
		arguments.put("-type", "JENKINS");

		boolean result = (ProcessLauncher.exec(arguments, build, launcher, listener) == 0);

		copyReportToTargetLocation(build, listener);
		deleteBuildFiles(build, listener);

		ReportPublisherAction action = new ReportPublisherAction(getCompareReportsDir(build));
		ReportFrameAction frameAction = new ReportFrameAction(getCompareReportsDir(build));
		build.addAction(action);
		build.addAction(frameAction);

		return result;
	}

	private void copyReportToTargetLocation(AbstractBuild<?, ?> build, BuildListener listener)
			throws IOException, InterruptedException {
		FilePath jobOutputDir = getTmpOut(build);
		File compareReportsDir = getCompareReportsDir(build);
		try {
			compareReportsDir.mkdirs();
		} catch (SecurityException e) {
			throw new RuntimeException("Unable to create " + compareReportsDir.getPath());
		}

		listener.getLogger().println(new Localizable(ResourceBundleHolder.get(MainConfiguration.class), "CopyingXtoY",
				jobOutputDir, compareReportsDir).toString());
		jobOutputDir.copyRecursiveTo(new FilePath(compareReportsDir));
		listener.getLogger().println(
				new Localizable(ResourceBundleHolder.get(MainConfiguration.class), "CopyingFinished").toString());
	}

	private void copyBuildFiles(AbstractBuild<?, ?> build, BuildListener listener)
			throws IOException, InterruptedException {
		FilePath compareOutputDir = FileUtils.getFilePath(build, inputFolder);
		FilePath workspaceInput = getTmpIn(build);
		listener.getLogger().println(new Localizable(ResourceBundleHolder.get(MainConfiguration.class), "CopyingXtoY",
				compareOutputDir, workspaceInput).toString());
		compareOutputDir.copyRecursiveTo(workspaceInput);
		listener.getLogger().println(
				new Localizable(ResourceBundleHolder.get(MainConfiguration.class), "CopyingFinished").toString());
	}

	private void deleteBuildFiles(AbstractBuild<?, ?> build, BuildListener listener)
			throws IOException, InterruptedException {
		FilePath workspaceInputDir = getTmpIn(build);
		FilePath workspaceOutputDir = getTmpOut(build);
		listener.getLogger().println(
				new Localizable(ResourceBundleHolder.get(MainConfiguration.class), "DeletingX", workspaceInputDir)
						.toString());
		workspaceInputDir.deleteRecursive();
		listener.getLogger().println(
				new Localizable(ResourceBundleHolder.get(MainConfiguration.class), "DeletingX", workspaceOutputDir)
						.toString());
		workspaceOutputDir.deleteRecursive();
	}

	// Overridden for better type safety.
	// If your plugin doesn't really define any property on Descriptor,
	// you don't have to do this.
	@Override
	public GenerateReportBuilderDescriptor getDescriptor() {
		// return new ToadBuilderDescriptor();
		return (GenerateReportBuilderDescriptor) super.getDescriptor();
	}

	/**
	 * Descriptor for {@link GenerateJenkinsReportBuilder}. Used as a singleton.
	 * The class is marked as public so that it can be accessed from views.
	 *
	 * <p>
	 * See
	 * <tt>src/main/resources/hudson/plugins/hello_world/HelloWorldBuilder/*.jelly</tt>
	 * for the actual HTML fragment for the configuration screen.
	 */
	@Extension
	public static class GenerateReportBuilderDescriptor extends BuildStepDescriptor<Builder> {

		/**
		 * In order to load the persisted global configuration, you have to call
		 * load() in the constructor.
		 */
		public GenerateReportBuilderDescriptor() {
			load();
		}

		public ListBoxModel doFillDatabaseSystemItems() {
		    ListBoxModel items = new ListBoxModel();
		    
		    for (DatabaseSystem s : DatabaseSystem.values()) {
		        items.add(s.getDisplayName(), s.name());
		    }
		    return items;
		}
		
		/**
		 * Performs on-the-fly validation of the form field 'inputFile'.
		 *
		 * @param value
		 *            This parameter receives the value that the user has typed.
		 * @return Indicates the outcome of the validation. This is sent to the
		 *         browser.
		 *         <p>
		 *         Note that returning {@link FormValidation#error(String)} does
		 *         not prevent the form from being saved. It just means that a
		 *         message will be displayed to the user.
		 */
		public FormValidation doCheckInputFolder(@QueryParameter String value) {
			FormValidation emptyValidation = FormValidationUtil.doCheckEmptyValue(value,
					new Localizable(ResourceBundleHolder.get(MainConfiguration.class), "InputFolderLocation")
							.toString());
			if (emptyValidation != FormValidation.ok()) {
				return emptyValidation;
			}
			return FormValidation.ok();
		}

		public boolean isApplicable(@SuppressWarnings("rawtypes") Class<? extends AbstractProject> aClass) {

			return true;
		}

		@Override
		public String getDisplayName() {
			return new Localizable(ResourceBundleHolder.get(MainConfiguration.class), "GenerateJenkinsReport")
					.toString();
		}
	}
}
