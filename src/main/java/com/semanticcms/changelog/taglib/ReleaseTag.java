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
package com.semanticcms.changelog.taglib;

import com.aoindustries.lang.NotImplementedException;
import com.aoindustries.sql.SQLUtility;
import static com.aoindustries.taglib.AttributeUtils.resolveValue;
import com.semanticcms.core.servlet.CaptureLevel;
import com.semanticcms.core.servlet.Link;
import static com.semanticcms.core.servlet.PageContext.getResponse;
import static com.semanticcms.core.servlet.PageContextEncoder.encodeTextInXhtml;
import static com.semanticcms.core.servlet.PageContextEncoder.encodeTextInXhtmlAttribute;
import static com.semanticcms.core.servlet.PageContextWriter.print;
import com.semanticcms.core.servlet.PageUtils;
import com.semanticcms.news.servlet.News;
import com.semanticcms.section.servlet.Section;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import javax.el.ELContext;
import javax.el.ValueExpression;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.SkipPageException;
import javax.servlet.jsp.tagext.JspFragment;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import org.joda.time.ReadableDateTime;

public class ReleaseTag extends SimpleTagSupport {

	private static final String SNAPSHOT_END = "-SNAPSHOT";

	private static final String GITHUB_START = "https://github.com/";

	private ValueExpression projectNameExpr;
	public void setProjectName(ValueExpression projectName) {
		this.projectNameExpr = projectName;
	}

	private ValueExpression versionExpr;
	public void setVersion(ValueExpression version) {
		this.versionExpr = version;
	}

	private ValueExpression dateCreatedExpr;
	public void setDateCreated(ValueExpression dateCreated) {
		this.dateCreatedExpr = dateCreated;
	}

	private ValueExpression groupIdExpr;
	public void setGroupId(ValueExpression groupId) {
		this.groupIdExpr = groupId;
	}

	private ValueExpression artifactIdExpr;
	public void setArtifactId(ValueExpression artifactId) {
		this.artifactIdExpr = artifactId;
	}

	private ValueExpression repositoryExpr;
	public void setRepository(ValueExpression repository) {
		this.repositoryExpr = repository;
	}

	private ValueExpression tagExpr;
	public void setTag(ValueExpression tag) {
		this.tagExpr = tag;
	}

	private ValueExpression scmUrlExpr;
	public void setScmUrl(ValueExpression scmUrl) {
		this.scmUrlExpr = scmUrl;
	}

	@Override
	public void doTag() throws JspException, IOException {
		PageContext pageContext = (PageContext)getJspContext();
		HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
		final CaptureLevel captureLevel = CaptureLevel.getCaptureLevel(request);
		if(captureLevel.compareTo(CaptureLevel.META) >= 0) {
			// Resolve attributes
			ELContext elContext = pageContext.getELContext();
			final String projectName = resolveValue(this.projectNameExpr, String.class, elContext);
			final String version = resolveValue(this.versionExpr, String.class, elContext);
			final ReadableDateTime dateCreated = PageUtils.toDateTime(resolveValue(this.dateCreatedExpr, Object.class, elContext));
			final String groupId = resolveValue(this.groupIdExpr, String.class, elContext);
			final String artifactId = resolveValue(this.artifactIdExpr, String.class, elContext);
			final String repository = resolveValue(this.repositoryExpr, String.class, elContext);
			String tag = resolveValue(this.tagExpr, String.class, elContext);
			final String scmUrl = resolveValue(this.scmUrlExpr, String.class, elContext);
			// Check rules between attribute values vs documented in semanticcms-changelog.tld
			boolean isSnapshot = version.endsWith(SNAPSHOT_END);
			if(!isSnapshot && dateCreated == null) {
				throw new JspTagException("dateCreated required for non-snapshot releases");
			}
			if(groupId != null && artifactId == null) {
				throw new JspException("artifactId required when groupId provided");
			}
			if(artifactId != null && groupId == null) {
				throw new JspException("groupId required when artifactId provided");
			}
			if(repository != null && (groupId == null || artifactId == null)) {
				throw new JspException("Both groupId and artifactId required when repository provided");
			}
			if(artifactId == null && tag == null) {
				throw new JspException("tag required when artifactId is not provided");
			}
			// Create tag if not provided
			final String tagName = tag!=null ? tag : (artifactId + '-' + version);
			// Create elements via servlet API
			final PrintWriter pw = new PrintWriter(pageContext.getOut()) {
				@Override
				public void flush() {
					// Avoid "Illegal to flush within a custom tag" from BodyContentImpl
				}
			};
			try {
				new Section(
					pageContext.getServletContext(),
					request,
					new HttpServletResponseWrapper((HttpServletResponse)pageContext.getResponse()) {
						@Override
						public PrintWriter getWriter() {
							return pw;
						}
						@Override
						public ServletOutputStream getOutputStream() {
							throw new NotImplementedException();
						}
					},
					tagName
				).id("version-" + version).invoke(() -> {
					if(dateCreated != null && captureLevel == CaptureLevel.BODY) {
						print("<footer><time itemprop=\"datePublished\" datetime=\"");
						encodeTextInXhtmlAttribute(dateCreated.toString());
						print("\">");
						encodeTextInXhtml(SQLUtility.getDate(dateCreated.getMillis()));
						print("</time></footer>\n");
					}
					if(!isSnapshot) new News(dateCreated, projectName + " " + version + " released.").invoke();
					new Section(isSnapshot ? "Snapshot Links" : "Release Links").id("release-links-" + version).invoke(() -> {
						if(captureLevel == CaptureLevel.BODY) {
							print("<ul>\n"
								+ "<li>");
							new Link().element("release-notes-" + version).invoke(() -> {
								print(isSnapshot ? "Snapshot Notes" : "Release Notes");
							});
							print("</li>\n");
							if(repository != null) {
								// Custom Maven Repository
								print("<li><a href=\"");
								encodeTextInXhtmlAttribute(repository);
								if(!repository.endsWith("/")) print('/');
								encodeTextInXhtmlAttribute(groupId.replace('.', '/'));
								print('/');
								print(artifactId);
								print('/');
								print(version);
								print("/\">Maven Repository</a></li>\n");
							} else if(groupId != null) {
								if(isSnapshot) {
									// Sonatype snapshots
									print("<li><a href=\"https://oss.sonatype.org/content/repositories/snapshots/");
									encodeTextInXhtmlAttribute(groupId.replace('.', '/'));
									print('/');
									print(artifactId);
									print('/');
									print(version);
									print("/\">Sonatype OSS Snapshot Repository</a></li>\n");
								} else {
									// Maven Central Repository
									print("<li><a href=\"https://search.maven.org/#");
									encodeTextInXhtmlAttribute(
										URLEncoder.encode(
											"artifactdetails|"
											+ groupId
											+ '|' + artifactId
											+ '|' + version
											+ '|',
											getResponse().getCharacterEncoding()
										)
									);
									print("\">Maven Central Repository</a></li>\n");
								}
							}
							if(scmUrl != null) {
								print("<li><a href=\"");
								encodeTextInXhtmlAttribute(scmUrl);
								if(!isSnapshot) {
									if(!scmUrl.endsWith("/")) print('/');
									print("releases/tag/");
									encodeTextInXhtmlAttribute(tagName);
								}
								print("\">");
								if(scmUrl.startsWith(GITHUB_START)) {
									print("GitHub");
								} else {
									print("Git");
								}
								print("</a></li>\n");
							}
							print("</ul>\n");
						}
					});
					new Section(isSnapshot ? "Snapshot Notes" : "Release Notes").id("release-notes-" + version).invoke(() -> {
						JspFragment body = getJspBody();
						if(body != null) {
							try {
								body.invoke(com.semanticcms.core.servlet.PageContext.getOut());
							} catch(SkipPageException e) {
								throw e;
							} catch(JspException e) {
								throw new ServletException(e);
							}
						}
					});
				});
			} catch(ServletException e) {
				throw new JspTagException(e);
			}
			if(pw.checkError()) throw new IOException("Error on doView PrintWriter");
		}
	}
}
