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
 * Builder implementation used to define "Deploy script" build step.
 *
 * @author pchudani
 */
public class DeployScriptBuilder extends Builder {

	private String out;
	private String in;
	private static final String IN = "IN";
	private static final String OUT = "OUT";

	// Fields in config.jelly must match the parameter names in the
	// "DataBoundConstructor"
	@DataBoundConstructor
	public DeployScriptBuilder(String out, String in) {
		this.out = out;
		this.in = in;
	}

	/**
	 * @return path to target database connection file. used from the
	 *         <tt>config.jelly</tt> to display on build step.
	 */
	public String getOut() {
		return out;
	}

	/**
	 * @return SQL script file location. used from the <tt>config.jelly</tt> to
	 *         display on build step.
	 */
	public String getIn() {
		return in;
	}
	

	private FilePath getTmpIn(AbstractBuild<?, ?> build) {
		return new FilePath(build.getWorkspace(), IN + build.number);
	}

	private FilePath getTmpOut(AbstractBuild<?, ?> build) {
		return new FilePath(build.getWorkspace(), OUT + build.number);
	}

	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
			throws IOException, InterruptedException {
		System.setOut(listener.getLogger());
		System.setErr(listener.getLogger());

		copyBuildFiles(build, listener);

		Map<String, String> arguments = new HashMap<>();
		arguments.put("-in", getTmpIn(build).toURI().getPath());
		arguments.put("-out", getTmpOut(build).toURI().getPath());
		arguments.put("-deploy", "");

		boolean result = (ProcessLauncher.exec(arguments, build, launcher, listener) == 0);

		deleteBuildFiles(build, listener);

		return result;
	}

	private void copyBuildFiles(AbstractBuild<?, ?> build, BuildListener listener)
			throws IOException, InterruptedException {
		FilePath input = FileUtils.getFilePath(build, in);
		FilePath targetConnection = FileUtils.getFilePath(build, out);

		FilePath workspaceTarget = getTmpOut(build);
		listener.getLogger().println(new Localizable(ResourceBundleHolder.get(MainConfiguration.class), "CopyingXtoY",
				targetConnection, workspaceTarget).toString());
		targetConnection.copyTo(workspaceTarget);
		listener.getLogger().println(
				new Localizable(ResourceBundleHolder.get(MainConfiguration.class), "CopyingFinished").toString());
		FilePath workspaceInput = getTmpIn(build);
		listener.getLogger().println(
				new Localizable(ResourceBundleHolder.get(MainConfiguration.class), "CopyingXtoY", input, workspaceInput)
						.toString());
		input.copyTo(workspaceInput);
		listener.getLogger().println(
				new Localizable(ResourceBundleHolder.get(MainConfiguration.class), "CopyingFinished").toString());
	}

	private void deleteBuildFiles(AbstractBuild<?, ?> build, BuildListener listener)
			throws IOException, InterruptedException {
		FilePath workspaceInput = getTmpIn(build);
		FilePath targetConnection = getTmpOut(build);

		listener.getLogger().println(
				new Localizable(ResourceBundleHolder.get(MainConfiguration.class), "DeletingX", targetConnection)
						.toString());
		targetConnection.delete();
		listener.getLogger()
				.println(new Localizable(ResourceBundleHolder.get(MainConfiguration.class), "DeletingX", workspaceInput)
						.toString());
		workspaceInput.delete();
	}

	// Overridden for better type safety.
	// If your plugin doesn't really define any property on Descriptor,
	// you don't have to do this.
	@Override
	public ToadBuilderDescriptor getDescriptor() {
		// return new ToadBuilderDescriptor();
		return (ToadBuilderDescriptor) super.getDescriptor();
	}

	/**
	 * Descriptor for {@link DeployScriptBuilder}. Used as a singleton. The
	 * class is marked as public so that it can be accessed from views.
	 *
	 * <p>
	 * See
	 * <tt>src/main/resources/hudson/plugins/hello_world/HelloWorldBuilder/*.jelly</tt>
	 * for the actual HTML fragment for the configuration screen.
	 */
	@Extension
	public static class ToadBuilderDescriptor extends BuildStepDescriptor<Builder> {

		/**
		 * In order to load the persisted global configuration, you have to call
		 * load() in the constructor.
		 */
		public ToadBuilderDescriptor() {
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
		 * Performs on-the-fly validation of the form field 'target'.
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
		public FormValidation doCheckOut(@QueryParameter String value) {
			FormValidation emptyValidation = FormValidationUtil.doCheckEmptyValue(value,
					new Localizable(ResourceBundleHolder.get(MainConfiguration.class), "TargetConnectionFile")
							.toString());
			if (emptyValidation != FormValidation.ok()) {
				return emptyValidation;
			}
			return FormValidation.ok();
		}

		/**
		 * Performs on-the-fly validation of the form field 'in'.
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
		public FormValidation doCheckIn(@QueryParameter String value) {
			FormValidation emptyValidation = FormValidationUtil.doCheckEmptyValue(value,
					new Localizable(ResourceBundleHolder.get(MainConfiguration.class), "InputFileLocation").toString());
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
			return new Localizable(ResourceBundleHolder.get(MainConfiguration.class), "DeployScript").toString();
		}
	}
}
