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

import org.jvnet.localizer.Localizable;
import org.jvnet.localizer.ResourceBundleHolder;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import hudson.Extension;
import hudson.util.FormValidation;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONObject;

@Extension(ordinal=110)
/**
 * Common global configuration
 * 
 * @author pchudani
 *
 */
public class MainConfiguration extends GlobalConfiguration {

	public MainConfiguration() {
		load();
	}
	
	/**
	 * Libraries location
	 */
	private String libs;
	
	/**
	 * This human readable name is used in the configuration screen.
	 */
	public String getDisplayName() {
		return "Continuous Integration with Toad Edge";
	}
	
	@Override
	public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
		libs = formData.getString("libs");
		
		save();
		
		return super.configure(req, formData);
	}
	
	/**
	 * 
	 * @return libraries path stored in global configuration
	 */
	public String getLibs() {
		return libs;
	}
	
	/**
	 * Performs on-the-fly validation of the form field 'libs'.
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
	public FormValidation doCheckLibs(@QueryParameter String value) {
		FormValidation emptyValidation = FormValidationUtil.doCheckEmptyValue(value, new Localizable(ResourceBundleHolder.get(MainConfiguration.class), "LibrariesFolder").toString());
		if (emptyValidation != FormValidation.ok()) {
			return emptyValidation;
		}
		File file = new File(value);
		if (!file.exists()) {
			return FormValidation.error(new Localizable(ResourceBundleHolder.get(MainConfiguration.class), "FileNonExistent").toString());
		}
		
		if (!value.endsWith(".zip")) {
			return FormValidation.error(new Localizable(ResourceBundleHolder.get(MainConfiguration.class), "ExpectingZippedLibs").toString());
		}
		
		return FormValidation.ok();
	}
	
	/**
	 * Performs on-the-fly validation of the form field 'license'.
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
	public FormValidation doCheckLicense(@QueryParameter String value) {
		FormValidation emptyValidation = FormValidationUtil.doCheckEmptyValue(value, new Localizable(ResourceBundleHolder.get(MainConfiguration.class), "LicenseKey").toString());
		if (emptyValidation != FormValidation.ok()) {
			return emptyValidation;
		}
		
		return FormValidation.ok();
	}
}
