/*
 * QUEST SOFTWARE PROPRIETARY INFORMATION
 *
 * This software is confidential.  Quest Software Inc., or one of its
 * subsidiaries, has supplied this software to you under terms of a
 * license agreement, nondisclosure agreement or both.
 *
 * You may not copy, disclose, or use this software except in accordance with
 * those terms.
 *
 *
 * Copyright 2017 Quest Software Inc.
 * ALL RIGHTS RESERVED.
 *
 * QUEST SOFTWARE INC. MAKES NO REPRESENTATIONS OR
 * WARRANTIES ABOUT THE SUITABILITY OF THE SOFTWARE,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE IMPLIED WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, OR
 * NON-INFRINGEMENT.  QUEST SOFTWARE SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE
 * AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
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
