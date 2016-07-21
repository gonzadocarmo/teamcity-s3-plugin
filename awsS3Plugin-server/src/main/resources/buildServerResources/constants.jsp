<%@ page import="com.dcn.teamcity.awsS3Plugin.PluginConstants" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="UI_PARAM_BUCKET_NAME" value="<%=PluginConstants.UI_PARAM_BUCKET_NAME%>"/>
<c:set var="UI_PARAM_BUCKET_NAME_DESCRIPTION" value="<%=PluginConstants.UI_PARAM_BUCKET_NAME_DESCRIPTION%>"/>

<c:set var="UI_PARAM_BUCKET_REGION" value="<%=PluginConstants.UI_PARAM_BUCKET_REGION%>"/>
<c:set var="UI_PARAM_BUCKET_REGION_DESCRIPTION" value="<%=PluginConstants.UI_PARAM_BUCKET_REGION_DESCRIPTION%>"/>

<c:set var="UI_PARAM_CREDENTIALS_PUB_KEY" value="<%=PluginConstants.UI_PARAM_CREDENTIALS_PUB_KEY%>"/>
<c:set var="UI_PARAM_CREDENTIALS_PUB_KEY_DESCRIPTION" value="<%=PluginConstants.UI_PARAM_CREDENTIALS_PUB_KEY_DESCRIPTION%>"/>

<c:set var="UI_PARAM_CREDENTIALS_PRIVATE_KEY" value="<%=PluginConstants.UI_PARAM_CREDENTIALS_PRIVATE_KEY%>"/>
<c:set var="UI_PARAM_CREDENTIALS_PRIVATE_KEY_DESCRIPTION" value="<%=PluginConstants.UI_PARAM_CREDENTIALS_PRIVATE_KEY_DESCRIPTION%>"/>

<c:set var="UI_PARAM_CONTENT_PATHS" value="<%=PluginConstants.UI_PARAM_CONTENT_PATHS%>"/>
<c:set var="UI_PARAM_CONTENT_PATHS_DESCRIPTION" value="<%=PluginConstants.UI_PARAM_CONTENT_PATHS_DESCRIPTION%>"/>

<c:set var="UI_PARAM_EMPTY_BUCKET" value="<%=PluginConstants.UI_PARAM_EMPTY_BUCKET%>"/>
<c:set var="UI_PARAM_EMPTY_BUCKET_DESCRIPTION" value="<%=PluginConstants.UI_PARAM_EMPTY_BUCKET_DESCRIPTION%>"/>

<c:set var="UI_PARAM_HTTP_PROXY" value="<%=PluginConstants.UI_PARAM_HTTP_PROXY%>"/>
<c:set var="UI_PARAM_HTTP_PROXY_DESCRIPTION" value="<%=PluginConstants.UI_PARAM_HTTP_PROXY_DESCRIPTION%>"/>

<c:set var="UI_PARAM_HTTP_HEADERS_CACHE_CONTROL" value="<%=PluginConstants.UI_PARAM_HTTP_HEADERS_CACHE_CONTROL%>"/>
<c:set var="UI_PARAM_HTTP_HEADERS_CACHE_CONTROL_DESCRIPTION" value="<%=PluginConstants.UI_PARAM_HTTP_HEADERS_CACHE_CONTROL_DESCRIPTION%>"/>