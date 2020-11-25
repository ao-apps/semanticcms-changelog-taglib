/*
 * semanticcms-changelog-taglib - Taglib for managing changelogs in a JSP environment.
 * Copyright (C) 2016, 2017, 2019, 2020  AO Industries, Inc.
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

import com.aoindustries.net.URIEncoder;
import static com.aoindustries.taglib.AttributeUtils.resolveValue;
import com.semanticcms.core.servlet.CaptureLevel;
import com.semanticcms.core.servlet.Link;
import static com.semanticcms.core.servlet.PageContextEncoder.encodeTextInXhtml;
import static com.semanticcms.core.servlet.PageContextEncoder.encodeTextInXhtmlAttribute;
import static com.semanticcms.core.servlet.PageContextWriter.print;
import com.semanticcms.core.servlet.PageUtils;
import com.semanticcms.news.servlet.News;
import com.semanticcms.section.servlet.Nav;
import com.semanticcms.section.servlet.Section;
import java.io.IOException;
import java.io.PrintWriter;
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
import org.apache.commons.lang3.NotImplementedException;
import org.joda.time.ReadableDateTime;
import org.joda.time.format.DateTimeFormat;

public class ReleaseTag extends SimpleTagSupport {

	private static final String SNAPSHOT_END = "-SNAPSHOT";

	private static final String VALIDATION_SNAPSHOT_END = "-validation" + SNAPSHOT_END;

	private static final String GITHUB_START = "https://github.com/";

	private ValueExpression projectNameExpr;
	public void setProjectName(ValueExpression projectName) {
		this.projectNameExpr = projectName;
	}

	private ValueExpression versionExpr;
	public void setVersion(ValueExpression version) {
		this.versionExpr = version;
	}

	private ValueExpression datePublishedExpr;
	public void setDatePublished(ValueExpression datePublished) {
		this.datePublishedExpr = datePublished;
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

	protected void printLinks(
		CaptureLevel captureLevel,
		String version,
		String groupId,
		String artifactId,
		String repository,
		String scmUrl,
		boolean isSnapshot,
		String tagName,
		String id,
		boolean absolute
	) throws IOException, ServletException, SkipPageException {
		if(captureLevel == CaptureLevel.BODY) {
			print("<ul>\n"
				+ "<li>");
			// TODO: Remove absolute here, and have absolute automatically added in RssServlet?
			new Link().element(id).absolute(absolute).invoke(() -> {
				print(isSnapshot ? "Snapshot Notes" : "Release Notes");
			});
			print("</li>\n");
			if(repository != null) {
				// Custom Maven Repository
				print("<li><a href=\"");
				encodeTextInXhtmlAttribute(repository);
				if(!repository.endsWith("/")) print('/');
				encodeTextInXhtmlAttribute(URIEncoder.encodeURIComponent(groupId).replace('.', '/'));
				print('/');
				encodeTextInXhtmlAttribute(URIEncoder.encodeURIComponent(artifactId));
				print('/');
				encodeTextInXhtmlAttribute(URIEncoder.encodeURIComponent(version));
				print("/\">Maven Repository</a></li>\n");
			} else if(groupId != null) {
				if(isSnapshot) {
					// Sonatype snapshots
					print("<li><a href=\"https://oss.sonatype.org/content/repositories/snapshots/");
					encodeTextInXhtmlAttribute(URIEncoder.encodeURIComponent(groupId).replace('.', '/'));
					print('/');
					encodeTextInXhtmlAttribute(URIEncoder.encodeURIComponent(artifactId));
					print('/');
					encodeTextInXhtmlAttribute(URIEncoder.encodeURIComponent(version));
					print("/\">Sonatype OSS Snapshot Repository</a></li>\n");
				} else {
					// Maven Central Repository
					print("<li><a href=\"https://search.maven.org/#");
					encodeTextInXhtmlAttribute(
						URIEncoder.encodeURIComponent(
							"artifactdetails|"
							+ groupId
							+ '|' + artifactId
							+ '|' + version
							+ '|'
						)
					);
					print("\">Maven Central Repository</a></li>\n");
				}
			}
			if(scmUrl != null) {
				boolean isGithub = scmUrl.startsWith(GITHUB_START);
				print("<li><a href=\"");
				encodeTextInXhtmlAttribute(scmUrl);
				if(!isSnapshot) {
					if(!scmUrl.endsWith("/")) print('/');
					print(isGithub ? "releases/tag/" : "refs/tags/");
					encodeTextInXhtmlAttribute(URIEncoder.encodeURIComponent(tagName));
				}
				print("\">");
				print(isGithub ? "GitHub" : "Git");
				print("</a></li>\n");
			}
			print("</ul>\n");
		}
	}

	@Override
	public void doTag() throws JspException, IOException {
		PageContext pageContext = (PageContext)getJspContext();
		HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
		final CaptureLevel captureLevel = CaptureLevel.getCaptureLevel(request);
		if(captureLevel.compareTo(CaptureLevel.META) >= 0) {
			HttpServletResponse response = (HttpServletResponse)pageContext.getResponse();
			// Resolve attributes
			ELContext elContext = pageContext.getELContext();
			final String projectName = resolveValue(this.projectNameExpr, String.class, elContext);
			final String version = resolveValue(this.versionExpr, String.class, elContext);
			final ReadableDateTime datePublished = PageUtils.toDateTime(resolveValue(this.datePublishedExpr, Object.class, elContext));
			final String groupId = resolveValue(this.groupIdExpr, String.class, elContext);
			final String artifactId = resolveValue(this.artifactIdExpr, String.class, elContext);
			final String repository = resolveValue(this.repositoryExpr, String.class, elContext);
			String tag = resolveValue(this.tagExpr, String.class, elContext);
			final String scmUrl = resolveValue(this.scmUrlExpr, String.class, elContext);
			// Check rules between attribute values vs documented in semanticcms-changelog.tld
			boolean isSnapshot = version.endsWith(SNAPSHOT_END);
			if(!isSnapshot && datePublished == null) {
				throw new JspTagException("datePublished required for non-snapshot releases");
			}
			if(groupId != null && artifactId == null) {
				throw new JspTagException("artifactId required when groupId provided");
			}
			if(artifactId != null && groupId == null) {
				throw new JspTagException("groupId required when artifactId provided");
			}
			if(repository != null && (groupId == null || artifactId == null)) {
				throw new JspTagException("Both groupId and artifactId required when repository provided");
			}
			if(artifactId == null && tag == null) {
				throw new JspTagException("tag required when artifactId is not provided");
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
				String idVersion;
				if(version.endsWith(VALIDATION_SNAPSHOT_END)) {
					idVersion = version.substring(0, version.length() - VALIDATION_SNAPSHOT_END.length());
				} else if(version.endsWith(SNAPSHOT_END)) {
					idVersion = version.substring(0, version.length() - SNAPSHOT_END.length());
				} else {
					idVersion = version;
				}
				String id = Release.DEFAULT_ID_PREFIX + '-' + idVersion;
				new Section(
					pageContext.getServletContext(),
					request,
					new HttpServletResponseWrapper(response) {
						@Override
						public PrintWriter getWriter() {
							return pw;
						}
						@Override
						public ServletOutputStream getOutputStream() {
							throw new NotImplementedException("getOutputStream not expected");
						}
					},
					new Release(
						projectName,
						version,
						datePublished,
						groupId,
						artifactId,
						repository,
						scmUrl,
						isSnapshot,
						tagName
					),
					tagName
				).id(id).invoke(() -> {
					if(datePublished != null && captureLevel == CaptureLevel.BODY) {
						print("<footer><time itemprop=\"datePublished\" datetime=\"");
						encodeTextInXhtmlAttribute(datePublished.toString());
						print("\">");
						encodeTextInXhtml(DateTimeFormat.forStyle("L-").withLocale(response.getLocale()).print(datePublished));
						print("</time></footer>\n");
					}
					if(!isSnapshot) {
						// TODO: Why is element required?  I expect it to automatically find it's parent element.
						new News(datePublished, projectName + " " + version + " released.").element(id).invoke(() -> {
							// TODO: We want the links directly in the RSS feed, too.
							// TODO: Should the links be written into "description", with the current description becoming the "title"?
							// TODO: This would then cause the links to be directly inside the RSS feed?
							// TODO: If doing so, absolute links.
							// TODO: Alternatively, the RSS description could default to the body of the news, with automatic absolute link conversion.
							// TODO: Then the description here would instead be the title.
							printLinks(
								captureLevel,
								version,
								groupId,
								artifactId,
								repository,
								scmUrl,
								isSnapshot,
								tagName,
								id,
								true
							);
						});
					}
					new Nav(isSnapshot ? "Snapshot Links" : "Release Links").id("release-links-" + version).invoke(() -> {
						printLinks(
							captureLevel,
							version,
							groupId,
							artifactId,
							repository,
							scmUrl,
							isSnapshot,
							tagName,
							id,
							false
						);
					});
					JspFragment body = getJspBody();
					if(body != null) {
						new Section(isSnapshot ? "Snapshot Notes" : "Release Notes").id(Release.DEFAULT_ID_PREFIX + "-body-" + idVersion).invoke(() -> {
							try {
								body.invoke(com.semanticcms.core.servlet.PageContext.getOut());
							} catch(SkipPageException e) {
								throw e;
							} catch(JspException e) {
								throw new ServletException(e);
							}
						});
					}
				});
			} catch(ServletException e) {
				throw new JspTagException(e);
			}
			if(pw.checkError()) throw new IOException("Error on doView PrintWriter");
		}
	}
}
