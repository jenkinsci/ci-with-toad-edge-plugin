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


/**
 * Enumeration of supported databases
 *
 * @author pfarkas
 *
 */
public enum DatabaseSystem {
	/**
	 * MySQL Database
	 */
	MYSQL("MySQL"),
	/**
	 * MariaDB Database
	 */
	MARIADB("MariaDB");

	private String displayName;

	private DatabaseSystem(final String displayName) {
		this.displayName = displayName;
	}

	/**
	 * @return Display name for ui
	 */
	public String getDisplayName() {
		return displayName;
	}
}
