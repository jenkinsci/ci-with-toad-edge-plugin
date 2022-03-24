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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.jvnet.localizer.Localizable;
import org.jvnet.localizer.ResourceBundleHolder;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import com.google.common.io.Files;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;

/**
 * Builder implementation used to define "Run Compare" build step.
 *
 * @author pchudani
 */
public class CompareBuilder extends Builder {

	private String outputFolder;
	private String srcInputFileOrFolder;
	private String tgtInputFileOrFolder;
	private String configFile;
	private static final String SOURCE = "IN_SOURCE";
	private static final String TARGET = "IN_TARGET";
	private static final String CONFIG = "CONFIG";
	private static final String TMP_OUTPUT = "TMP_OUTPUT";

	// Fields in config.jelly must match the parameter names in the
	// "DataBoundConstructor"
	@DataBoundConstructor
	public CompareBuilder(String outputFolder, String srcInputType, String tgtInputType, String srcInputFileOrFolder,
			String tgtInputFileOrFolder, String configFile) {
		this.outputFolder = outputFolder;
		this.srcInputFileOrFolder = srcInputFileOrFolder;
		this.tgtInputFileOrFolder = tgtInputFileOrFolder;
		this.configFile = configFile;
	}

	/**
	 * @return Output folder name. used from the <tt>config.jelly</tt> to
	 *         display on build step.
	 */
	public String getOutputFolder() {
		return outputFolder;
	}

	/**
	 * @return Source input file or folder location. used from the
	 *         <tt>config.jelly</tt> to display on build step.
	 */
	public String getSrcInputFileOrFolder() {
		return srcInputFileOrFolder;
	}

	/**
	 * @return Target input file or folder location. used from the
	 *         <tt>config.jelly</tt> to display on build step.
	 */
	public String getTgtInputFileOrFolder() {
		return tgtInputFileOrFolder;
	}

	/**
	 * @return Configuration file location. used from the <tt>config.jelly</tt>
	 *         to display on build step.
	 */
	public String getConfigFile() {
		return configFile;
	}

	private FilePath getTmpInSource(AbstractBuild<?, ?> build) throws IOException, InterruptedException {
		FilePath sourceInput = FileUtils.getFilePath(build, srcInputFileOrFolder);
		if (sourceInput.isDirectory()) {
			return new FilePath(build.getWorkspace(), SOURCE + build.number);
		}
		return new FilePath(build.getWorkspace(),
				SOURCE + build.number + "." + Files.getFileExtension(sourceInput.getName()));
	}

	private FilePath getTmpInTarget(AbstractBuild<?, ?> build) throws IOException, InterruptedException {
		FilePath targetInput = FileUtils.getFilePath(build, tgtInputFileOrFolder);
		if (targetInput.isDirectory()) {
			return new FilePath(build.getWorkspace(), TARGET + build.number);
		}
		return new FilePath(build.getWorkspace(),
				TARGET + build.number + "." + Files.getFileExtension(targetInput.getName()));
	}

	private FilePath getTmpOutput(AbstractBuild<?, ?> build) {
		return new FilePath(build.getWorkspace(), TMP_OUTPUT + build.number);
	}

	private FilePath getTmpConfig(AbstractBuild<?, ?> build) {
		return new FilePath(build.getWorkspace(), CONFIG + build.number);
	}

	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
			throws IOException, InterruptedException {
		System.setOut(listener.getLogger());
		System.setErr(listener.getLogger());

		FormValidation checkValidation = FormValidationUtil.restrictLocation(srcInputFileOrFolder, build);
		if(checkValidation != FormValidation.ok()) {
			throw new Error(checkValidation.getMessage());
		}
		FormValidation checkValidationTarget = FormValidationUtil.restrictLocation(tgtInputFileOrFolder, build);
		if(checkValidationTarget != FormValidation.ok()) {
			throw new Error(checkValidationTarget.getMessage());
		}
		FormValidation checkValidationConfig=FormValidationUtil.restrictLocation(configFile, build);
		if(checkValidationConfig != FormValidation.ok()){
			throw new Error(checkValidationConfig.getMessage());
		}

		copyBuildFiles(build, listener);
		ensureTmpOutputFolder(build, listener);

		Map<String, String> arguments = new HashMap<>();
		arguments.put("-in_source", getTmpInSource(build).toURI().getPath());
		arguments.put("-in_target", getTmpInTarget(build).toURI().getPath());
		arguments.put("-out", getTmpOutput(build).toURI().getPath());
		if (configFile != null && !configFile.isEmpty()) {
			arguments.put("-settings", getTmpConfig(build).toURI().getPath());
		}
		arguments.put("-compare", "");

		boolean result = (ProcessLauncher.exec(arguments, build, launcher, listener) == 0);

		copyOutputFromTmp(build, listener);
		deleteBuildFiles(build, listener);

		return result;
	}

	private void copyOutputFromTmp(AbstractBuild<?, ?> build, BuildListener listener)
			throws IOException, InterruptedException {
		FilePath tmpOutput = getTmpOutput(build);
		FilePath output = FileUtils.getFilePath(build, outputFolder);
		if (!output.exists()) {
			output.mkdirs();
		}
		listener.getLogger().println(
				new Localizable(ResourceBundleHolder.get(MainConfiguration.class), "CopyingXtoY", tmpOutput, output)
						.toString());
		tmpOutput.copyRecursiveTo(output);
		listener.getLogger().println(
				new Localizable(ResourceBundleHolder.get(MainConfiguration.class), "CopyingFinished").toString());
	}

	private void ensureTmpOutputFolder(AbstractBuild<?, ?> build, BuildListener listener)
			throws IOException, InterruptedException {
		FilePath tmpOutput = getTmpOutput(build);

		if (!tmpOutput.exists()) {
			listener.getLogger().println(
					new Localizable(ResourceBundleHolder.get(MainConfiguration.class), "CreatingTmpFolder", tmpOutput)
							.toString());
			tmpOutput.mkdirs();
		}
	}

	private void copyBuildFiles(AbstractBuild<?, ?> build, BuildListener listener)
			throws IOException, InterruptedException {
		FilePath sourceInput = FileUtils.getFilePath(build, srcInputFileOrFolder);
		FilePath targetInput = FileUtils.getFilePath(build, tgtInputFileOrFolder);
		FilePath config = FileUtils.getFilePath(build, configFile);

		if (sourceInput.isDirectory()) {
			FilePath workspaceSource = getTmpInSource(build);
			listener.getLogger().println(new Localizable(ResourceBundleHolder.get(MainConfiguration.class),
					"CopyingXtoY", sourceInput, workspaceSource).toString());
			sourceInput.copyRecursiveTo(workspaceSource);
			listener.getLogger().println(
					new Localizable(ResourceBundleHolder.get(MainConfiguration.class), "CopyingFinished").toString());
		} else {
			FilePath workspaceSource = getTmpInSource(build);
			listener.getLogger().println(new Localizable(ResourceBundleHolder.get(MainConfiguration.class),
					"CopyingXtoY", sourceInput, workspaceSource).toString());
			sourceInput.copyTo(workspaceSource);
			listener.getLogger().println(
					new Localizable(ResourceBundleHolder.get(MainConfiguration.class), "CopyingFinished").toString());
		}

		if (targetInput.isDirectory()) {
			FilePath workspaceTarget = getTmpInTarget(build);
			listener.getLogger().println(new Localizable(ResourceBundleHolder.get(MainConfiguration.class),
					"CopyingXtoY", targetInput, workspaceTarget).toString());
			targetInput.copyRecursiveTo(workspaceTarget);
			listener.getLogger().println(
					new Localizable(ResourceBundleHolder.get(MainConfiguration.class), "CopyingFinished").toString());
		} else {
			FilePath workspaceTarget = getTmpInTarget(build);
			listener.getLogger().println(new Localizable(ResourceBundleHolder.get(MainConfiguration.class),
					"CopyingXtoY", targetInput, workspaceTarget).toString());
			targetInput.copyTo(workspaceTarget);
			listener.getLogger().println(
					new Localizable(ResourceBundleHolder.get(MainConfiguration.class), "CopyingFinished").toString());
		}

		if (configFile != null && !configFile.isEmpty()) {
			FilePath workspaceConfig = getTmpConfig(build);
			listener.getLogger().println(new Localizable(ResourceBundleHolder.get(MainConfiguration.class), "CopyingXtoY",
					config, workspaceConfig).toString());
			config.copyTo(workspaceConfig);
			listener.getLogger().println(
					new Localizable(ResourceBundleHolder.get(MainConfiguration.class), "CopyingFinished").toString());
		}
	}

	private void deleteBuildFiles(AbstractBuild<?, ?> build, BuildListener listener)
			throws IOException, InterruptedException {
		FilePath sourceInput = getTmpInSource(build);
		FilePath targetInput = getTmpInTarget(build);
		FilePath config = getTmpConfig(build);

		if (sourceInput.isDirectory()) {
			listener.getLogger().println(
					new Localizable(ResourceBundleHolder.get(MainConfiguration.class), "DeletingX", sourceInput)
							.toString());
			sourceInput.deleteRecursive();
		} else {
			listener.getLogger().println(
					new Localizable(ResourceBundleHolder.get(MainConfiguration.class), "DeletingX", sourceInput)
							.toString());
			sourceInput.delete();
		}

		if (targetInput.isDirectory()) {
			listener.getLogger().println(
					new Localizable(ResourceBundleHolder.get(MainConfiguration.class), "DeletingX", targetInput)
							.toString());
			targetInput.deleteRecursive();
		} else {
			listener.getLogger().println(
					new Localizable(ResourceBundleHolder.get(MainConfiguration.class), "DeletingX", targetInput)
							.toString());
			targetInput.delete();
		}
		
		if (configFile != null && !configFile.isEmpty()) {
			listener.getLogger().println(
					new Localizable(ResourceBundleHolder.get(MainConfiguration.class), "DeletingX", config).toString());
			config.delete();
		}

		FilePath output = getTmpOutput(build);

		listener.getLogger().println(
				new Localizable(ResourceBundleHolder.get(MainConfiguration.class), "DeletingX", output).toString());
		output.deleteRecursive();
	}

	// Overridden for better type safety.
	// If your plugin doesn't really define any property on Descriptor,
	// you don't have to do this.
	@Override
	public CompareBuilderDescriptor getDescriptor() {
		// return new ToadBuilderDescriptor();
		return (CompareBuilderDescriptor) super.getDescriptor();
	}

	/**
	 * Descriptor for {@link CompareBuilder}. Used as a singleton. The class is
	 * marked as public so that it can be accessed from views.
	 *
	 * <p>
	 * See
	 * <tt>src/main/resources/hudson/plugins/hello_world/HelloWorldBuilder/*.jelly</tt>
	 * for the actual HTML fragment for the configuration screen.
	 */
	@Extension
	public static class CompareBuilderDescriptor extends BuildStepDescriptor<Builder> {

		/**
		 * In order to load the persisted global configuration, you have to call
		 * load() in the constructor.
		 */
		public CompareBuilderDescriptor() {
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
		 * Performs on-the-fly validation of the form field 'outputFolder'.
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
		public FormValidation doCheckOutputFolder(@QueryParameter String value) {
			FormValidation emptyValidation = FormValidationUtil.doCheckEmptyValue(value,
					new Localizable(ResourceBundleHolder.get(MainConfiguration.class), "OutputFolderName").toString());
			if (emptyValidation != FormValidation.ok()) {
				return emptyValidation;
			}
			return FormValidation.ok();
		}

		private FormValidation doCheckInputFileOrFolder(String value) {
			FormValidation emptyValidation = FormValidationUtil.doCheckEmptyValue(value,
					new Localizable(ResourceBundleHolder.get(MainConfiguration.class), "InputFileOrFolderLocation")
							.toString());
			if (emptyValidation != FormValidation.ok()) {
				return emptyValidation;
			}
			return FormValidation.ok();
		}

		public FormValidation doCheckSrcInputFileOrFolder(@QueryParameter String value) {
			return doCheckInputFileOrFolder(value);
		}

		public FormValidation doCheckTgtInputFileOrFolder(@QueryParameter String value) {
			return doCheckInputFileOrFolder(value);
		}

		public boolean isApplicable(@SuppressWarnings("rawtypes") Class<? extends AbstractProject> aClass) {

			return true;
		}

		@Override
		public String getDisplayName() {
			return new Localizable(ResourceBundleHolder.get(MainConfiguration.class), "RunCompare").toString();
		}
	}
}
