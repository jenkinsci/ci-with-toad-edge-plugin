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
 * Builder implementation used to define "Create baseline" build step.
 *
 * @author pchudani
 */
public class CreateBaselineBuilder extends Builder {

	private String outputFile;
	private String inputFileOrFolder;
	private static final String INPUT = "INPUT";
	private static final String TMP_OUTPUT = "TMP_OUTPUT";

	// Fields in config.jelly must match the parameter names in the
	// "DataBoundConstructor"
	@DataBoundConstructor
	public CreateBaselineBuilder(String outputFile, String inputFileOrFolder) {
		this.outputFile = outputFile;
		this.inputFileOrFolder = inputFileOrFolder;
	}

	/**
	 * @return Output file location. used from the <tt>config.jelly</tt> to display on build step.
	 */
	public String getOutputFile() {
		return outputFile;
	}

	/**
	 * @return Input file or folder location. used from the <tt>config.jelly</tt> to display on build step.
	 */
	public String getInputFileOrFolder() {
		return inputFileOrFolder;
	}
	
	private FilePath getTmpInput(AbstractBuild<?, ?> build) throws IOException, InterruptedException {
		FilePath input = FileUtils.getFilePath(build, inputFileOrFolder);
		if (input.isDirectory()) {
			return new FilePath(build.getWorkspace(), INPUT + build.number);
		}
		return new FilePath(build.getWorkspace(), INPUT + build.number + "." + Files.getFileExtension(input.getName()));
	}
	
	private FilePath getTmpOutput(AbstractBuild<?, ?> build) throws IOException, InterruptedException {
		return new FilePath(build.getWorkspace(), TMP_OUTPUT + build.number);
	}

	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
		System.setOut(listener.getLogger());
		System.setErr(listener.getLogger());
		
		copyBuildFiles(build, listener);

		Map<String, String> arguments = new HashMap<>();
		arguments.put("-in",
				getTmpInput(build).toURI().getPath());
		arguments.put("-out", getTmpOutput(build).toURI().getPath());
		arguments.put("-baseline", "");

		boolean result = (ProcessLauncher.exec(arguments, build, launcher, listener) == 0);
	
		copySnapshotToTargetLocation(build, listener);
		deleteBuildFiles(build, listener);
		
		return result;
	}
	
	private void copyBuildFiles(AbstractBuild<?, ?> build, BuildListener listener) throws IOException, InterruptedException {
		FilePath input = FileUtils.getFilePath(build, inputFileOrFolder);

		if (input.isDirectory()) {
			FilePath workspaceInput = getTmpInput(build);
			listener.getLogger().println(new Localizable(ResourceBundleHolder.get(MainConfiguration.class), "CopyingXtoY", input, workspaceInput).toString());
			input.copyRecursiveTo(workspaceInput);
			listener.getLogger().println(new Localizable(ResourceBundleHolder.get(MainConfiguration.class),"CopyingFinished").toString());
		} else {
			FilePath workspaceInput = getTmpInput(build);
			listener.getLogger().println(new Localizable(ResourceBundleHolder.get(MainConfiguration.class), "CopyingXtoY", input, workspaceInput).toString());
			input.copyTo(workspaceInput);
			listener.getLogger().println(new Localizable(ResourceBundleHolder.get(MainConfiguration.class),"CopyingFinished").toString());
		}
	}
	
	private void copySnapshotToTargetLocation(AbstractBuild<?, ?> build, BuildListener listener) throws IOException, InterruptedException {
		FilePath tmpOutput = getTmpOutput(build);
		FilePath snapshotFile = FileUtils.getFilePath(build, outputFile);
		if (snapshotFile.getParent() != null) {
			snapshotFile.getParent().mkdirs();
		}
		
		listener.getLogger().println(new Localizable(ResourceBundleHolder.get(MainConfiguration.class), "CopyingXtoY", tmpOutput, snapshotFile).toString());
		tmpOutput.copyTo(snapshotFile);
		listener.getLogger().println(new Localizable(ResourceBundleHolder.get(MainConfiguration.class),"CopyingFinished").toString());
	}
	
	private void deleteBuildFiles(AbstractBuild<?, ?> build, BuildListener listener) throws IOException, InterruptedException {
		FilePath jobInput = getTmpInput(build);
		
		if (jobInput.isDirectory()) {
			listener.getLogger().println(new Localizable(ResourceBundleHolder.get(MainConfiguration.class), "DeletingX", jobInput).toString());
			jobInput.deleteRecursive();
		} else {
			listener.getLogger().println(new Localizable(ResourceBundleHolder.get(MainConfiguration.class), "DeletingX", jobInput).toString());
			jobInput.delete();
		}
		
		FilePath output = getTmpOutput(build);

		listener.getLogger().println(new Localizable(ResourceBundleHolder.get(MainConfiguration.class), "DeletingX", output).toString());
		output.delete();
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
	 * Descriptor for {@link CreateBaselineBuilder}. Used as a singleton. The
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
		 * Performs on-the-fly validation of the form field 'outputFile'.
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
		public FormValidation doCheckOutputFile(@QueryParameter String value) {
			FormValidation emptyValidation = FormValidationUtil.doCheckEmptyValue(value, new Localizable(ResourceBundleHolder.get(MainConfiguration.class), "OutputFile").toString());
			if (emptyValidation != FormValidation.ok()) {
				return emptyValidation;
			}
			return FormValidation.ok();
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
		public FormValidation doCheckInputFileOrFolder(@QueryParameter String value) {
			FormValidation emptyValidation = FormValidationUtil.doCheckEmptyValue(value, new Localizable(ResourceBundleHolder.get(MainConfiguration.class), "InputFileOrFolderLocation").toString());
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
			return new Localizable(ResourceBundleHolder.get(MainConfiguration.class), "TakeBaseline").toString();
		}
	}
}
