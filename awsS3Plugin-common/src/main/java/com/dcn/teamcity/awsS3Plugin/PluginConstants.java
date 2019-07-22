package com.dcn.teamcity.awsS3Plugin;

public interface PluginConstants {

    String UI_PARAM_BUCKET_NAME = "Bucket";
    String UI_PARAM_BUCKET_NAME_DESCRIPTION = "Name";

    String UI_PARAM_BUCKET_REGION = "region";
    String UI_PARAM_BUCKET_REGION_DESCRIPTION = "Region";

    String UI_PARAM_CREDENTIALS_PUB_KEY = "accessKeyId";
    String UI_PARAM_CREDENTIALS_PUB_KEY_DESCRIPTION = "AWS access key ID";

    String UI_PARAM_CREDENTIALS_PRIVATE_KEY = "secretAccessKey";
    String UI_PARAM_CREDENTIALS_PRIVATE_KEY_DESCRIPTION = "AWS secret access key";

    String UI_PARAM_CONTENT_PATHS = "content-paths";
    String UI_PARAM_CONTENT_PATHS_DESCRIPTION = "Artifacts path";

    String UI_PARAM_EMPTY_BUCKET = "empty-bucket";
    String UI_PARAM_EMPTY_BUCKET_DESCRIPTION = "Empty bucket before upload";

    String UI_PARAM_HTTP_PROXY = "http-proxy";
    String UI_PARAM_HTTP_PROXY_DESCRIPTION = "Http Proxy";

    String UI_PARAM_HTTP_HEADERS_CACHE_CONTROL = "http-headers-cache-control";
    String UI_PARAM_HTTP_HEADERS_CACHE_CONTROL_DESCRIPTION = "Cache-Control";

    String RUNNER_TYPE = "awsS3Plugin";
    String RUNNER_DESCRIPTION = "Runner for uploading content to Amazon S3";
    String RUNNER_DISPLAY_NAME = "Amazon S3 Uploader";
}
