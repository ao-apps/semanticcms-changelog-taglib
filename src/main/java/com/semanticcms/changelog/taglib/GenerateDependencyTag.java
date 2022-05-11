/*
 * semanticcms-changelog-taglib - Taglib for managing changelogs in a JSP environment.
 * Copyright (C) 2017, 2020, 2021, 2022  AO Industries, Inc.
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

import static com.aoapps.encoding.TextInJavaScriptEncoder.encodeTextInJavascript;
import static com.aoapps.encoding.TextInXhtmlAttributeEncoder.encodeTextInXhtmlAttribute;
import static com.aoapps.encoding.TextInXhtmlEncoder.encodeTextInXhtml;
import static com.aoapps.taglib.AttributeUtils.resolveValue;

import com.aoapps.encoding.MediaType;
import com.aoapps.encoding.taglib.EncodingNullTag;
import com.semanticcms.core.servlet.CaptureLevel;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.el.ELContext;
import javax.el.ValueExpression;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;

/**
 * Generates a dependency declaration for a given release and build system.
 */
// TODO: BodyTag
public class GenerateDependencyTag extends EncodingNullTag {

  private ValueExpression releaseExpr;

  public void setRelease(ValueExpression release) {
    this.releaseExpr = release;
  }

  private ValueExpression dependenciesExpr;

  public void setDependencies(ValueExpression dependencies) {
    this.dependenciesExpr = dependencies;
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
  protected void doTag(Writer out) throws JspException, IOException {
    PageContext pageContext = (PageContext) getJspContext();
    HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
    final CaptureLevel captureLevel = CaptureLevel.getCaptureLevel(request);
    if (captureLevel == CaptureLevel.BODY) {
      // Resolve attributes
      ELContext elContext = pageContext.getELContext();
      Release release = resolveValue(this.releaseExpr, Release.class, elContext);
      List<? extends Release> dependenciesList;
      if (dependenciesExpr == null) {
        dependenciesList = Collections.emptyList();
      } else {
        Object dependencies = resolveValue(this.dependenciesExpr, Object.class, elContext);
        if (dependencies == null) {
          dependenciesList = Collections.emptyList();
        } else if (dependencies instanceof Release) {
          dependenciesList = Collections.singletonList((Release) dependencies);
        } else if (dependencies instanceof Release[]) {
          dependenciesList = Arrays.asList((Release[]) dependencies);
        } else if (dependencies instanceof List<?>) {
          @SuppressWarnings("unchecked")
          List<? extends Release> asList = (List<? extends Release>) dependencies;
          dependenciesList = asList;
        } else if (dependencies instanceof Collection<?>) {
          @SuppressWarnings("unchecked")
          Collection<? extends Release> asCollection = (Collection<? extends Release>) dependencies;
          dependenciesList = new ArrayList<>(asCollection);
        } else if (dependencies instanceof Iterable<?>) {
          @SuppressWarnings("unchecked")
          Iterable<? extends Release> asIterable = (Iterable<? extends Release>) dependencies;
          Iterator<? extends Release> iter = asIterable.iterator();
          List<Release> asList = new ArrayList<>();
          while (iter.hasNext()) {
            asList.add(iter.next());
          }
          dependenciesList = asList;
        } else {
          throw new IllegalArgumentException("Unexpected type for dependencies: " + dependencies.getClass().getName());
        }
      }
      String buildSystem = resolveValue(this.buildSystemExpr, String.class, elContext);
      if (buildSystem != null) {
        buildSystem = buildSystem.trim();
      }
      if (
          buildSystem == null
              || buildSystem.isEmpty()
              || "Maven".equalsIgnoreCase(buildSystem)
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
        for (Release dependency : dependenciesList) {
          out.write(
              "\n<dependency>\n"
                  + "    <groupId>");
          encodeTextInXhtml(dependency.getGroupId(), out);
          out.write("</groupId>\n"
              + "    <artifactId>");
          encodeTextInXhtml(dependency.getArtifactId(), out);
          out.write("</artifactId>\n"
              + "    <version>");
          encodeTextInXhtml(dependency.getVersion(), out);
          out.write("</version>\n"
              + "</dependency>");
        }
      } else if ("Buildr".equalsIgnoreCase(buildSystem)) {
        out.write('\'');
        encodeTextInJavascript(release.getGroupId(), out);
        out.write(':');
        encodeTextInJavascript(release.getArtifactId(), out);
        out.write(":jar:");
        encodeTextInJavascript(release.getVersion(), out);
        out.write('\'');
        for (Release dependency : dependenciesList) {
          out.write(",\n'");
          encodeTextInJavascript(dependency.getGroupId(), out);
          out.write(':');
          encodeTextInJavascript(dependency.getArtifactId(), out);
          out.write(":jar:");
          encodeTextInJavascript(dependency.getVersion(), out);
          out.write('\'');
        }
      } else if ("Ivy".equalsIgnoreCase(buildSystem)) {
        out.write("<dependency org=\"");
        encodeTextInXhtmlAttribute(release.getGroupId(), out);
        out.write("\" name=\"");
        encodeTextInXhtmlAttribute(release.getArtifactId(), out);
        out.write("\" rev=\"");
        encodeTextInXhtmlAttribute(release.getVersion(), out);
        out.write("\" />");
        for (Release dependency : dependenciesList) {
          out.write("\n<dependency org=\"");
          encodeTextInXhtmlAttribute(dependency.getGroupId(), out);
          out.write("\" name=\"");
          encodeTextInXhtmlAttribute(dependency.getArtifactId(), out);
          out.write("\" rev=\"");
          encodeTextInXhtmlAttribute(dependency.getVersion(), out);
          out.write("\" />");
        }
      } else if ("Grape".equalsIgnoreCase(buildSystem)) {
        out.write(
            "@Grapes(\n"
                + "    @Grab(group='");
        encodeTextInJavascript(release.getGroupId(), out);
        out.write("', module='");
        encodeTextInJavascript(release.getArtifactId(), out);
        out.write("', version='");
        encodeTextInJavascript(release.getVersion(), out);
        out.write(
            "')\n");
        for (Release dependency : dependenciesList) {
          out.write("    @Grab(group='");
          encodeTextInJavascript(dependency.getGroupId(), out);
          out.write("', module='");
          encodeTextInJavascript(dependency.getArtifactId(), out);
          out.write("', version='");
          encodeTextInJavascript(dependency.getVersion(), out);
          out.write(
              "')\n");
        }
        out.write(')');
      } else if ("Grails".equalsIgnoreCase(buildSystem)) {
        out.write("compile '");
        encodeTextInJavascript(release.getGroupId(), out);
        out.write(':');
        encodeTextInJavascript(release.getArtifactId(), out);
        out.write(':');
        encodeTextInJavascript(release.getVersion(), out);
        out.write('\'');
        for (Release dependency : dependenciesList) {
          out.write("\ncompile '");
          encodeTextInJavascript(dependency.getGroupId(), out);
          out.write(':');
          encodeTextInJavascript(dependency.getArtifactId(), out);
          out.write(':');
          encodeTextInJavascript(dependency.getVersion(), out);
          out.write('\'');
        }
      } else if ("SBT".equalsIgnoreCase(buildSystem)) {
        out.write("libraryDependencies += \"");
        encodeTextInJavascript(release.getGroupId(), out);
        out.write("\" % \"");
        encodeTextInJavascript(release.getArtifactId(), out);
        out.write("\" % \"");
        encodeTextInJavascript(release.getVersion(), out);
        out.write('"');
        for (Release dependency : dependenciesList) {
          out.write("\nlibraryDependencies += \"");
          encodeTextInJavascript(dependency.getGroupId(), out);
          out.write("\" % \"");
          encodeTextInJavascript(dependency.getArtifactId(), out);
          out.write("\" % \"");
          encodeTextInJavascript(dependency.getVersion(), out);
          out.write('"');
        }
      } else if ("Leiningen".equalsIgnoreCase(buildSystem)) {
        out.write('[');
        out.write(release.getGroupId());
        out.write('/');
        out.write(release.getArtifactId());
        out.write(" \"");
        encodeTextInJavascript(release.getVersion(), out);
        out.write("\"]");
        for (Release dependency : dependenciesList) {
          out.write("\n[");
          out.write(dependency.getGroupId());
          out.write('/');
          out.write(dependency.getArtifactId());
          out.write(" \"");
          encodeTextInJavascript(dependency.getVersion(), out);
          out.write("\"]");
        }
      } else {
        throw new JspTagException("Unexpected buildSystem: " + buildSystem);
      }
    }
  }
}
