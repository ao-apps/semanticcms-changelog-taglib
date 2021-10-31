/*
 * semanticcms-changelog-taglib - Taglib for managing changelogs in a JSP environment.
 * Copyright (C) 2017, 2020  AO Industries, Inc.
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

import com.semanticcms.core.model.Element;
import com.semanticcms.core.model.Node;
import java.util.Collection;
import java.util.Iterator;

public class Functions {

	public static Release findRelease(Node node) {
		Release[] firstSnapshot = new Release[1];
		Release release = findReleaseRecurse(node, firstSnapshot);
		return release != null ? release : firstSnapshot[0];
	}

	@SuppressWarnings("null")
	private static Release findReleaseRecurse(Node node, Release[] firstSnapshot) {
		if(node instanceof Release) {
			Release release = (Release)node;
			if(!release.getIsSnapshot()) return release;
			if(firstSnapshot[0] == null) firstSnapshot[0] = release;
		}
		for(Element child : node.getChildElements()) {
			Release release = findReleaseRecurse(child, firstSnapshot);
			if(release != null) return release;
		}
		return null;
	}

	public static int countDependencies(Object dependencies) {
		if(dependencies == null) {
			return 0;
		} else if(dependencies instanceof Release) {
			return 1;
		} else if(dependencies instanceof Release[]) {
			return ((Release[])dependencies).length;
		} else if(dependencies instanceof Collection<?>) {
			return ((Collection<?>)dependencies).size();
		} else if(dependencies instanceof Iterable) {
			int count = 0;
			@SuppressWarnings("unchecked")
			Iterator<? extends Release> iter = ((Iterable<? extends Release>)dependencies).iterator();
			while(iter.hasNext()) {
				Release release = iter.next();
				count++;
			}
			return count;
		} else {
			throw new IllegalArgumentException("Unexpected type for dependencies: " + dependencies.getClass().getName());
		}
	}

	private Functions() {}
}
