package com.dcn.teamcity.awsS3Plugin;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import jetbrains.buildServer.BuildProblemData;
import jetbrains.buildServer.BuildProblemTypes;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.*;

/**
 * Created by sg0216948 on 7/12/16.
 *
 * @author <a href="mailto:gonzalo.docarmo@gmail.com">Gonzalo G. do Carmo Norte</a>
 */
public class AWSS3BuildProcessAdapterHelperTest {

    private AWSS3BuildProcessAdapterHelper helper;

    @BeforeMethod
    public void setUp() throws Exception {
        helper = new AWSS3BuildProcessAdapterHelper();
    }

    @Test
    public void testCreateBuildProblemDataWithFilePath() throws Exception {
        final String bucket_name = "my-bucket-with-long-name";
        final String file_path = "/Users/sg0216948/Downloads/TeamCity/buildAgent/work/56eb2e988bd79b91/.gitignore";
        final String exception_message = "my-bucket";

        BuildProblemData result = helper.createBuildProblemData(bucket_name, file_path, exception_message);

        System.out.println(result.getIdentity());
        System.out.println(result.getIdentity().length());

        assertTrue(result.getIdentity().contains("my-bucket-w" + ":" + "rk/56eb2e988bd79b91/.gitignore" + ":"));

        assertEquals(result.getType(), BuildProblemTypes.TC_ERROR_MESSAGE_TYPE);
        assertEquals(result.getDescription(), exception_message);
    }

    @Test
    public void testCreateBuildProblemData() throws Exception {
        final String bucket_name = "my-bucket";
        final String exception_message = "Access Denied (Service: Amazon S3; Status Code: 403;";

        BuildProblemData result = helper.createBuildProblemData(bucket_name, exception_message);

        assertTrue(result.getIdentity().contains(bucket_name + ":"));
        assertEquals(result.getType(), BuildProblemTypes.TC_ERROR_MESSAGE_TYPE);
        assertEquals(result.getDescription(), exception_message);
    }

    @Test
    public void testCreateClient() throws Exception {
        final String publicKey = "AAA";
        final String privateKey = "ZZZ";

        AmazonS3 result = helper.createClient(publicKey, privateKey);
        assertTrue(result instanceof AmazonS3Client);
    }

    @Test
    public void testCreateClientWithProxy() throws Exception {
        final String publicKey = "AAA";
        final String privateKey = "ZZZ";
        final String proxyUrl = "http://localhost:3128";

        AmazonS3 result = helper.createClientWithProxy(publicKey, privateKey, proxyUrl);
        assertTrue(result instanceof AmazonS3Client);
    }

    @Test
    public void createClientConfigurationBasicProxyHTTP() throws Exception {
        final String proxy_protocol = "http";
        final String proxy_host = "localhost";
        final int proxy_port = 3128;

        final String proxyString = String.format("%s://%s:%s",
                proxy_protocol,
                proxy_host,
                proxy_port
        );

        ClientConfiguration result = helper.createClientConfiguration(proxyString);

        assertEquals(result.getProtocol().toString(), proxy_protocol);

        assertEquals(result.getProxyHost(), proxy_host);
        assertEquals(result.getProxyPort(), proxy_port);

        assertNull(result.getProxyDomain());
        assertNull(result.getProxyUsername());
        assertNull(result.getProxyPassword());
    }

    @Test
    public void createClientConfigurationBasicProxyHTTPS() throws Exception {
        final String proxy_protocol = "https";
        final String proxy_host = "localhost";
        final int proxy_port = 3128;

        final String proxyString = String.format("%s://%s:%s",
                proxy_protocol,
                proxy_host,
                proxy_port
        );

        ClientConfiguration result = helper.createClientConfiguration(proxyString);

        assertEquals(result.getProtocol().toString(), proxy_protocol);

        assertEquals(result.getProxyHost(), proxy_host);
        assertEquals(result.getProxyPort(), proxy_port);

        assertNull(result.getProxyDomain());
        assertNull(result.getProxyUsername());
        assertNull(result.getProxyPassword());
    }

    @Test
    public void createClientConfigurationProxyWithUsernameNoPass() throws Exception {
        final String proxy_protocol = "http";
        final String proxy_host = "www-my-proxy.hostname.com";
        final int proxy_port = 80;
        final String proxy_user = "username";

        final String proxyString = String.format("%s://%s@%s:%s",
                proxy_protocol,
                proxy_user,
                proxy_host,
                proxy_port
        );

        ClientConfiguration result = helper.createClientConfiguration(proxyString);

        assertEquals(result.getProtocol().toString(), proxy_protocol);
        assertNull(result.getProxyDomain());

        assertEquals(result.getProxyHost(), proxy_host);
        assertEquals(result.getProxyPort(), proxy_port);

        assertEquals(result.getProxyUsername(), proxy_user);
        assertNull(result.getProxyPassword());
    }

    @Test
    public void createClientConfigurationProxyWithUsernameAndPass() throws Exception {
        final String proxy_protocol = "http";
        final String proxy_host = "www-my-proxy.hostname.com";
        final int proxy_port = 80;
        final String proxy_user = "username";
        final String proxy_pass = "password";

        final String proxyString = String.format("%s://%s:%s@%s:%s",
                proxy_protocol,
                proxy_user,
                proxy_pass,
                proxy_host,
                proxy_port
        );

        ClientConfiguration result = helper.createClientConfiguration(proxyString);

        assertEquals(result.getProtocol().toString(), proxy_protocol);
        assertNull(result.getProxyDomain());

        assertEquals(result.getProxyHost(), proxy_host);
        assertEquals(result.getProxyPort(), proxy_port);

        assertEquals(result.getProxyUsername(), proxy_user);
        assertEquals(result.getProxyPassword(), proxy_pass);
    }

    @Test
    public void createClientConfigurationProxyWithUserDomain() throws Exception {
        final String proxy_protocol = "http";
        final String proxy_domain = "global";
        final String proxy_host = "www-my-proxy.hostname.com";
        final int proxy_port = 80;
        final String proxy_user = "username";
        final String proxy_pass = "password";

        final String proxyString = String.format("%s://%s\\%s:%s@%s:%s",
                proxy_protocol,
                proxy_domain,
                proxy_user,
                proxy_pass,
                proxy_host,
                proxy_port
        );

        ClientConfiguration result = helper.createClientConfiguration(proxyString);

        assertEquals(result.getProtocol().toString(), proxy_protocol);
        assertEquals(result.getProxyDomain(), proxy_domain);

        assertEquals(result.getProxyHost(), proxy_host);
        assertEquals(result.getProxyPort(), proxy_port);

        assertEquals(result.getProxyUsername(), proxy_user);
        assertEquals(result.getProxyPassword(), proxy_pass);
    }

    @Test
    public void testCreateModel() throws Exception {
        final String valueSpacesTrimmed = "someValue";
        Map<String, String> input = new HashMap();
        input.put(PluginConstants.UI_PARAM_BUCKET_NAME, "someValue   ");
        input.put(PluginConstants.UI_PARAM_BUCKET_REGION, "   someValue");
        input.put(PluginConstants.UI_PARAM_CREDENTIALS_PUB_KEY, "   someValue    ");
        input.put(PluginConstants.UI_PARAM_CREDENTIALS_PRIVATE_KEY, "  someValue           ");
        input.put(PluginConstants.UI_PARAM_EMPTY_BUCKET, "true");
        input.put(PluginConstants.UI_PARAM_HTTP_HEADERS_CACHE_CONTROL, "  someValue ");
        input.put(PluginConstants.UI_PARAM_HTTP_PROXY, "  someValue ");

        AgentRunnerBuildParametersModel result = helper.createModel(input);

        assertEquals(result.getBucketName(), valueSpacesTrimmed);
        assertEquals(result.getPublicKey(), valueSpacesTrimmed);
        assertEquals(result.getPrivateKey(), valueSpacesTrimmed);
        assertTrue(result.isEmptyBucketBeforeUpload());
        assertEquals(result.getHttpHeaderCacheControl(), valueSpacesTrimmed);
        assertEquals(result.getHttpProxy(), valueSpacesTrimmed);
    }

    @Test
    public void testCreateModelNoDelete() throws Exception {
        final String valueSpacesTrimmed = "someValue";
        Map<String, String> input = new HashMap();
        input.put(PluginConstants.UI_PARAM_BUCKET_NAME, "someValue   ");
        input.put(PluginConstants.UI_PARAM_BUCKET_REGION, "   someValue");
        input.put(PluginConstants.UI_PARAM_CREDENTIALS_PUB_KEY, "   someValue    ");
        input.put(PluginConstants.UI_PARAM_CREDENTIALS_PRIVATE_KEY, "  someValue           ");
        input.put(PluginConstants.UI_PARAM_EMPTY_BUCKET, "false");
        input.put(PluginConstants.UI_PARAM_HTTP_HEADERS_CACHE_CONTROL, "  someValue ");

        AgentRunnerBuildParametersModel result = helper.createModel(input);

        assertEquals(result.getBucketName(), valueSpacesTrimmed);
        assertEquals(result.getPublicKey(), valueSpacesTrimmed);
        assertEquals(result.getPrivateKey(), valueSpacesTrimmed);
        assertFalse(result.isEmptyBucketBeforeUpload());
        assertEquals(result.getHttpHeaderCacheControl(), valueSpacesTrimmed);
    }

    @Test
    public void testCreateModelNotRequiredFields() throws Exception {
        Map<String, String> input = new HashMap();
        input.put(PluginConstants.UI_PARAM_HTTP_HEADERS_CACHE_CONTROL, "   ");

        AgentRunnerBuildParametersModel result = helper.createModel(input);

        assertNull(result.getHttpHeaderCacheControl());
    }
}