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
