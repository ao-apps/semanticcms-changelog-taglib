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
 * along with semanticcms-changelog-taglib.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.semanticcms.changelog.taglib;

import com.aoindustries.encoding.MediaType;
import static com.aoindustries.encoding.TextInJavaScriptEncoder.encodeTextInJavaScript;
import static com.aoindustries.encoding.TextInXhtmlAttributeEncoder.encodeTextInXhtmlAttribute;
import static com.aoindustries.encoding.TextInXhtmlEncoder.encodeTextInXhtml;
import static com.aoindustries.taglib.AttributeUtils.resolveValue;
import com.aoindustries.taglib.AutoEncodingNullTag;
import com.semanticcms.core.servlet.CaptureLevel;
import java.io.IOException;
import java.io.Writer;
import javax.el.ELContext;
import javax.el.ValueExpression;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;

public class GenerateDependencyTag extends AutoEncodingNullTag {

	private ValueExpression releaseExpr;
	public void setRelease(ValueExpression release) {
		this.releaseExpr = release;
	}

	private ValueExpression buildSystemExpr;
	public void setBuildSystem(ValueExpression buildSystem) {
		this.buildSystemExpr = buildSystem;
	}

	@Override
	public MediaType getOutputType() {
		return MediaType.TEXT;
	}

	@Override
	protected void doTag(Writer out) throws JspTagException, IOException {
		PageContext pageContext = (PageContext)getJspContext();
		HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
		final CaptureLevel captureLevel = CaptureLevel.getCaptureLevel(request);
		if(captureLevel == CaptureLevel.BODY) {
			// Resolve attributes
			ELContext elContext = pageContext.getELContext();
			Release release = resolveValue(this.releaseExpr, Release.class, elContext);
			String buildSystem = resolveValue(this.buildSystemExpr, String.class, elContext);
			if(buildSystem != null) buildSystem = buildSystem.trim();
			if(
				buildSystem == null
				|| buildSystem.isEmpty()
				|| buildSystem.equalsIgnoreCase("Maven")
			) {
				out.write(
					"<dependency>\n"
					+ "    <groupId>");
				encodeTextInXhtml(release.getGroupId(), out);
				out.write("</groupId>\n"
					+ "    <artifactId>");
				encodeTextInXhtml(release.getArtifactId(), out);
				out.write("</artifactId>\n"
					+ "    <version>");
				encodeTextInXhtml(release.getVersion(), out);
				out.write("</version>\n"
					+ "</dependency>");
			} else if(buildSystem.equalsIgnoreCase("Buildr")) {
				out.write('\'');
				encodeTextInJavaScript(release.getGroupId(), out);
				out.write(':');
				encodeTextInJavaScript(release.getArtifactId(), out);
				out.write(":jar:");
				encodeTextInJavaScript(release.getVersion(), out);
				out.write('\'');
			} else if(buildSystem.equalsIgnoreCase("Ivy")) {
				out.write("<dependency org=\"");
				encodeTextInXhtmlAttribute(release.getGroupId(), out);
				out.write("\" name=\"");
				encodeTextInXhtmlAttribute(release.getArtifactId(), out);
				out.write("\" rev=\"");
				encodeTextInXhtmlAttribute(release.getVersion(), out);
				out.write("\" />");
			} else if(buildSystem.equalsIgnoreCase("Grape")) {
				out.write(
					"@Grapes(\n"
					+ "@Grab(group='");
				encodeTextInJavaScript(release.getGroupId(), out);
				out.write("', module='");
				encodeTextInJavaScript(release.getArtifactId(), out);
				out.write("', version='");
				encodeTextInJavaScript(release.getVersion(), out);
				out.write(
					"')\n"
					+ ")");
			} else if(buildSystem.equalsIgnoreCase("Grails")) {
				out.write("compile '");
				encodeTextInJavaScript(release.getGroupId(), out);
				out.write(':');
				encodeTextInJavaScript(release.getArtifactId(), out);
				out.write(':');
				encodeTextInJavaScript(release.getVersion(), out);
				out.write('\'');
			} else if(buildSystem.equalsIgnoreCase("SBT")) {
				out.write("libraryDependencies += \"");
				encodeTextInJavaScript(release.getGroupId(), out);
				out.write("\" % \"");
				encodeTextInJavaScript(release.getArtifactId(), out);
				out.write("\" % \"");
				encodeTextInJavaScript(release.getVersion(), out);
				out.write('"');
			} else if(buildSystem.equalsIgnoreCase("Leiningen")) {
				out.write('[');
				out.write(release.getGroupId());
				out.write('/');
				out.write(release.getArtifactId());
				out.write(" \"");
				encodeTextInJavaScript(release.getVersion(), out);
				out.write("\"]");
			} else {
				throw new JspTagException("Unexpected buildSystem: " + buildSystem);
			}
		}
	}
}
