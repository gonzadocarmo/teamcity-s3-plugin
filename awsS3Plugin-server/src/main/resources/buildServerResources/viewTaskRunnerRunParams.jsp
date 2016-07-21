<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<%@include file="constants.jsp" %>

<div class="parameter">
   Bucket <c:out value="${UI_PARAM_BUCKET_NAME_DESCRIPTION}" />:
   <strong><props:displayValue name="${UI_PARAM_BUCKET_NAME}"
                                            emptyValue="default"/></strong>
</div>

<div class="parameter">
   Bucket <c:out value="${UI_PARAM_BUCKET_REGION_DESCRIPTION}" />:
   <strong><props:displayValue name="${UI_PARAM_BUCKET_REGION}" emptyValue="default"/></strong>
</div>

<div class="parameter">
   <c:out value="${UI_PARAM_CREDENTIALS_PUB_KEY_DESCRIPTION}" />:
   <strong><props:displayValue name="${UI_PARAM_CREDENTIALS_PUB_KEY}"
                                         emptyValue="none"/></strong>
</div>

<div class="parameter">
   <c:out value="${UI_PARAM_CONTENT_PATHS_DESCRIPTION}" />:
   <strong><props:displayValue name="${UI_PARAM_CONTENT_PATHS}"
                                       emptyValue="none"/></strong>
</div>

<div class="parameter">
   <c:out value="${UI_PARAM_EMPTY_BUCKET_DESCRIPTION}" />:
   <strong>
   <c:choose>
      <c:when test="${propertiesBean.properties[UI_PARAM_EMPTY_BUCKET]}">
         <c:out value="Yes"/>
      </c:when>
      <c:otherwise>
         <c:out value="No"/>
      </c:otherwise>
   </c:choose>
   </strong>
</div>