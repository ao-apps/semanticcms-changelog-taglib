/*
 * semanticcms-changelog-taglib - Taglib for managing changelogs in a JSP environment.
 * Copyright (C) 2017  AO Industries, Inc.
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
 * along with semanticcms-changelog-taglib.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.semanticcms.changelog.taglib;

import com.semanticcms.section.model.Section;
import org.joda.time.ReadableDateTime;

public class Release extends Section {

	static final String DEFAULT_ID_PREFIX = "release-notes";

	private final String projectName;
	public String getProjectName() {
		return projectName;
	}

	private final String version;
	public String getVersion() {
		return version;
	}

	private final ReadableDateTime datePublished;
	public ReadableDateTime getDatePublished() {
		return datePublished;
	}

	private final String groupId;
	public String getGroupId() {
		return groupId;
	}

	private final String artifactId;
	public String getArtifactId() {
		return artifactId;
	}

	private final String repository;
	public String getRepository() {
		return repository;
	}

	private final String scmUrl;
	public String getScmUrl() {
		return scmUrl;
	}

	private final boolean isSnapshot;
	public boolean getIsSnapshot() {
		return isSnapshot;
	}

	private final String tagName;
	public String getTagName() {
		return tagName;
	}

	public Release(
		String projectName,
		String version,
		ReadableDateTime datePublished,
		String groupId,
		String artifactId,
		String repository,
		String scmUrl,
		boolean isSnapshot,
		String tagName
	) {
		this.projectName = projectName;
		this.version = version;
		this.datePublished = datePublished;
		this.groupId = groupId;
		this.artifactId = artifactId;
		this.repository = repository;
		this.scmUrl = scmUrl;
		this.isSnapshot = isSnapshot;
		this.tagName = tagName;
	}

	@Override
	protected String getDefaultIdPrefix() {
		return DEFAULT_ID_PREFIX;
	}
}
