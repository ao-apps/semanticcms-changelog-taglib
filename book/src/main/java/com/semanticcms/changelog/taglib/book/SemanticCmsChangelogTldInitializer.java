/*
 * semanticcms-changelog-taglib - Taglib for managing changelogs in a JSP environment.
 * Copyright (C) 2016  AO Industries, Inc.
 *     support@aoindustries.com
 *     7262 Bull Pen Cir
 *     Mobile, AL 36695
 *
 * This file is part of semanticcms-changelog-taglib.
 *
 * semanticcms-changelog-taglib is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * semanticcms-changelog-taglib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with semanticcms-changelog-taglib.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.semanticcms.changelog.taglib.book;

import com.semanticcms.tagreference.TagReferenceInitializer;
import java.util.Collections;

/**
 * @author  AO Industries, Inc.
 */
public class SemanticCmsChangelogTldInitializer extends TagReferenceInitializer {

	public SemanticCmsChangelogTldInitializer() {
		super(
			"Changelog Taglib Reference",
			"Taglib Reference",
			"/changelog/taglib",
			"/semanticcms-changelog.tld",
			"https://docs.oracle.com/javase/8/docs/api/",
			"https://docs.oracle.com/javaee/6/api/",
			Collections.singletonMap("com.semanticcms.changelog.taglib.", "https://semanticcms.com/changelog/taglib/apidocs/")
		);
	}
}
