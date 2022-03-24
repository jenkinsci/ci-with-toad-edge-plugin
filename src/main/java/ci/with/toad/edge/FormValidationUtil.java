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

import hudson.FilePath;
import hudson.model.AbstractBuild;
import org.jvnet.localizer.Localizable;
import org.jvnet.localizer.ResourceBundleHolder;

import hudson.util.FormValidation;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

public class FormValidationUtil {
	
	/**
	 * 
	 * @param value - value to check
	 * @param fieldName - name of the field to check
	 * @return validation error if field is empty, OK status otherwise
	 */
	public static FormValidation doCheckEmptyValue(String value, String fieldName) {
		if (value.length() == 0) {
			return FormValidation.error(String.format(new Localizable(ResourceBundleHolder.get(MainConfiguration.class), "XMustNotBeEmpty").toString(), fieldName));
		}
		return FormValidation.ok();
	}

	/**
	 * Checks if folder path is in current workspace path
	 *
	 * @param folderPath
	 * @return
	 */
	@Nonnull
	public static FormValidation restrictLocation(String folderPath, AbstractBuild<?, ?> build) throws IOException, InterruptedException {
        final FilePath workspaceDir = build.getWorkspace();
		final FilePath inputDir = FileUtils.getFilePath(build,folderPath);
		if(workspaceDir == null) {
			return FormValidation.ok();
		}
		final String inputDirLocation = inputDir.toURI().getPath();
		final String workspaceDirLocation = workspaceDir.toURI().getPath();

		if(workspaceDir.isRemote() != inputDir.isRemote()) {
           return FormValidation.error(String.format("Input folder: %s  needs to be in the job's agent node", inputDirLocation));
		}
		if(inputDirLocation.contains(workspaceDirLocation)) {
			return FormValidation.ok();
		}
		return FormValidation.error(String.format("Specified folder or file (%s) should be inside project workspace directory",inputDirLocation));
	}
}
