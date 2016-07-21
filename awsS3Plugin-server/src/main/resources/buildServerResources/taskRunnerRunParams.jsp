<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<%@include file="constants.jsp" %>

<l:settingsGroup title="Amazon S3 bucket">
    <tr>
        <th>
            <label for="${UI_PARAM_BUCKET_NAME}">
                <c:out value="${UI_PARAM_BUCKET_NAME_DESCRIPTION}"/>:
                <l:star/>
            </label>
        </th>
        <td>
            <props:textProperty name="${UI_PARAM_BUCKET_NAME}" className="longField"/>
            <span class="error" id="error_${UI_PARAM_BUCKET_NAME}"></span>
        </td>
    </tr>

    <tr>
        <th>
            <label for="${UI_PARAM_BUCKET_REGION}">
                <c:out value="${UI_PARAM_BUCKET_REGION_DESCRIPTION}"/>:
                <l:star/>
            </label>
        </th>
        <td>
            <props:textProperty name="${UI_PARAM_BUCKET_REGION}" className="longField"/>
            <span class="error" id="error_${UI_PARAM_BUCKET_REGION}"></span>
            <span class="smallNote">
                E.g. 'us-east-1' or 'us-west-2'.
            </span>
        </td>
    </tr>
</l:settingsGroup>

<l:settingsGroup title="AWS Credentials">
    <tr>
        <th>
            <label for="${UI_PARAM_CREDENTIALS_PUB_KEY}">
                <c:out value="${UI_PARAM_CREDENTIALS_PUB_KEY_DESCRIPTION}"/>:
                <l:star/>
            </label>
        </th>
        <td>
            <props:textProperty name="${UI_PARAM_CREDENTIALS_PUB_KEY}" className="longField"/>
            <span class="error" id="error_${UI_PARAM_CREDENTIALS_PUB_KEY}"></span>
        </td>
    </tr>

    <tr>
        <th>
            <label for="${UI_PARAM_CREDENTIALS_PRIVATE_KEY}">
                <c:out value="${UI_PARAM_CREDENTIALS_PRIVATE_KEY_DESCRIPTION}"/>:
                <l:star/>
            </label>
        </th>
        <td>
            <props:passwordProperty name="${UI_PARAM_CREDENTIALS_PRIVATE_KEY}" className="longField"/>
            <span class="error" id="error_${UI_PARAM_CREDENTIALS_PRIVATE_KEY}"></span>
        </td>
    </tr>
</l:settingsGroup>

<l:settingsGroup title="Source Content">
    <tr>
        <th>
            <label for="${UI_PARAM_CONTENT_PATHS}">
                <c:out value="${UI_PARAM_CONTENT_PATHS_DESCRIPTION}"/>:
                <l:star/>
            </label>
        </th>
        <td>
            <props:multilineProperty name="${UI_PARAM_CONTENT_PATHS}" className="longField"
                                     cols="30" rows="4" expanded="true" linkTitle="Enter artifacts paths"/>
            <span class="error" id="error_${UI_PARAM_CONTENT_PATHS}"></span>
            <span class="smallNote">
                New line or comma separated paths to build artifacts.
                Ant-style wildcards like dir/**/*.zip and target directories like *.zip => winFiles,unix/distro.tgz => linuxFiles, where winFiles and linuxFiles are target directories are supported.
            </span>
        </td>
    </tr>
</l:settingsGroup>

<l:settingsGroup title="Bucket Content" className="advancedSetting">
    <tr class="advancedSetting">
        <th>
            <label for="${UI_PARAM_EMPTY_BUCKET}">
                <c:out value="${UI_PARAM_EMPTY_BUCKET_DESCRIPTION}"/>:
            </label>
        </th>
        <td>
            <c:set var="modeSelected_emptyBucket" value="${propertiesBean.properties[UI_PARAM_EMPTY_BUCKET]}"/>
            <props:checkboxProperty name="${UI_PARAM_EMPTY_BUCKET}"
                                    checked="${modeSelected_emptyBucket}"/>
            <span class="smallNote">
                Select this option if the bucket content is required to be removed before upload
            </span>
        </td>
    </tr>

    <tr class="advancedSetting">
        <th>
            <label for="${UI_PARAM_HTTP_HEADERS_CACHE_CONTROL}">
                <c:out value="${UI_PARAM_HTTP_HEADERS_CACHE_CONTROL_DESCRIPTION}"/> HTTP Header:
            </label>
        </th>
        <td>
            <props:textProperty name="${UI_PARAM_HTTP_HEADERS_CACHE_CONTROL}" className="longField"/>
            <span class="smallNote">
                Specify caching behavior.
                <a class="helpIcon"
                   onclick="BS.Util.showHelp(event, 'https://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.9', {width: 0, height: 0}); return false"
                   style="" href="https://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.9"
                   title="View help" showdiscardchangesmessage="false">
                    <bs:helpIcon/>
                </a>
                <br/>
                E.g. 'max-age=21600' / 'public, max-age=604800'.
            </span>
        </td>
    </tr>
</l:settingsGroup>

<l:settingsGroup title="Connection" className="advancedSetting">
    <tr class="advancedSetting">
        <th>
            <label for="${UI_PARAM_HTTP_PROXY}">
                <c:out value="${UI_PARAM_HTTP_PROXY_DESCRIPTION}"/>:
            </label>
        </th>
        <td>
            <props:textProperty name="${UI_PARAM_HTTP_PROXY}" className="longField"/>
            <span class="error" id="error_${UI_PARAM_HTTP_PROXY}"></span>
            <span class="smallNote">
                E.g. 'http://localhost:3128' or 'http://user:pass@www.domain.com:3128' or 'http://domain\user:pass@www.domain.com:3128'
            </span>
        </td>
    </tr>
</l:settingsGroup>

