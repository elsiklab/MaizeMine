<%--
  - Author: Fengyuan Hu
  - Date: 11-06-2010
  - Copyright Notice:
  - @(#)  Copyright (C) 2002-2010 FlyMine

          This code may be freely distributed and modified under the
          terms of the GNU Lesser General Public Licence.  This should
          be distributed with the code.  See the LICENSE file for more
          information or http://www.gnu.org/copyleft/lesser.html.

  - Description: In this page, it displays the results of overlapping
                 located sequence features with the constrains and spans
                 by users'setup
  --%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<!--  spanUploadResults.jsp -->

<html:xhtml />

<script language="javascript">

</script>

<p align="center"><h1>Result</h1></p>

<div id="errorMessage">
  <c:if test="${!empty errorMsg}">
    <p>${errorMsg}</p>
  </c:if>
</div>

<div>

<table cellspacing="0" cellpadding="0" border="0" align="center" >
<tr>
  <td valign="top">
  <table cellpadding="0" cellspacing="0" border="0" class="dbsources">

    <tr valign="top">
      <th>Span</th>
      <th>Feature PID</th>
      <th>Feature Type</th>
      <th>Location</th>
      <th>Number of Matched Bases</th>
      <th>Submission DCCid</th>
    </tr>

    <c:forEach var="element" items="${results}">

    <tr>
      <td rowspan="${fn:length(element.value)}">
        <c:out value="${element.key.chr}:${element.key.start}..${element.key.end}"/>
      </td>

      <c:forEach var="result" begin="0" end="0" items="${element.value}">
          <td><a href="${WEB_PROPERTIES['webapp.baseurl']}/${WEB_PROPERTIES['webapp.path']}/portal.do?externalid=${result[0]}&class=${fn:split(result[2].name,".")[fn:length(fn:split(result[2].name,"."))-1]}"><c:out value="${result[0]}"/></a></td>
          <td><c:out value="${fn:split(result[2].name,\".\")[fn:length(fn:split(result[2].name,\".\"))-1]}"/></td>
          <td><c:out value="${result[3]}:${result[4]}..${result[5]}"/></td>
          <td>
            <c:choose>
              <c:when test="${result[4] <= element.key.start && result[5] >= element.key.start && result[5] <= element.key.end}">
                <c:out value="${result[5]-element.key.start+1}"/>
              </c:when>
              <c:when test="${result[4] >= element.key.start && result[4] <= element.key.end && result[5] >= element.key.end}">
                <c:out value="${element.key.end-result[4]+1}"/>
              </c:when>
              <c:when test="${result[4] >= element.key.start && result[5] <= element.key.end}">
                <c:out value="${result[5]-result[4]+1}"/>
              </c:when>
              <c:when test="${result[4] <= element.key.start && result[5] >= element.key.end}">
                <c:out value="${element.key.end-element.key.start+1}"/>
              </c:when>
            </c:choose>
          </td>
          <td><a href="${WEB_PROPERTIES['webapp.baseurl']}/${WEB_PROPERTIES['webapp.path']}/portal.do?externalid=${result[6]}&class=Submission"><c:out value="${result[6]}"/></a></td>
      </c:forEach>
    </tr>

    <c:forEach var="result" begin="1" end="${fn:length(element.value)-1}" items="${element.value}">
        <tr>
          <td><a href="${WEB_PROPERTIES['webapp.baseurl']}/${WEB_PROPERTIES['webapp.path']}/portal.do?externalid=${result[0]}&class=${fn:split(result[2].name,".")[fn:length(fn:split(result[2].name,"."))-1]}"><c:out value="${result[0]}"/></a></td>
          <td><c:out value="${fn:split(result[2].name,\".\")[fn:length(fn:split(result[2].name,\".\"))-1]}"/></td>
          <td><c:out value="${result[3]}:${result[4]}..${result[5]}"/></td>
          <td>
            <c:choose>
              <c:when test="${result[4] <= element.key.start && result[5] >= element.key.start && result[5] <= element.key.end}">
                <c:out value="${result[5]-element.key.start+1}"/>
              </c:when>
              <c:when test="${result[4] >= element.key.start && result[4] <= element.key.end && result[5] >= element.key.end}">
                <c:out value="${element.key.end-result[4]+1}"/>
              </c:when>
              <c:when test="${result[4] >= element.key.start && result[5] <= element.key.end}">
                <c:out value="${result[5]-result[4]+1}"/>
              </c:when>
              <c:when test="${result[4] <= element.key.start && result[5] >= element.key.end}">
                <c:out value="${element.key.end-element.key.start+1}"/>
              </c:when>
            </c:choose>
          </td>
          <td><a href="${WEB_PROPERTIES['webapp.baseurl']}/${WEB_PROPERTIES['webapp.path']}/portal.do?externalid=${result[6]}&class=Submission"><c:out value="${result[6]}"/></a></td>
        </tr>
    </c:forEach>

    </c:forEach>

  </table>
  </td>
</tr>
</table>

</div>


<!--  /spanUploadResults.jsp -->