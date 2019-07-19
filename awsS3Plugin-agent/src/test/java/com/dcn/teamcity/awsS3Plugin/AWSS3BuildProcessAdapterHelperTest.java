package com.dcn.teamcity.awsS3Plugin;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import jetbrains.buildServer.BuildProblemData;
import jetbrains.buildServer.BuildProblemTypes;
import jetbrains.buildServer.agent.BuildProgressLogger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.*;

/**
 *
 * @author <a href="mailto:gonzalo.docarmo@gmail.com">Gonzalo G. do Carmo Norte</a>
 */
public class AWSS3BuildProcessAdapterHelperTest {

    private AWSS3BuildProcessAdapterHelper helper;
    private BuildProgressLogger myLogger;

    @BeforeMethod
    public void setUp() throws Exception {
        myLogger = mock(BuildProgressLogger.class);
        helper = new AWSS3BuildProcessAdapterHelper().withLogger(myLogger);
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
        final String region = "us-west-2";

        AmazonS3 result = helper.createClient(publicKey, privateKey, region);
        assertTrue(result instanceof AmazonS3Client);
        assertEquals(result.getRegion().toAWSRegion().getName(), Regions.US_WEST_2.getName());
    }

    @Test
    public void testCreateClientFailureWhenInvalidRegion() throws Exception {
        final String publicKey = "AAA";
        final String privateKey = "ZZZ";
        final String region = "invalid-region-name";

        AmazonS3 result = helper.createClient(publicKey, privateKey, region);
        assertTrue(result instanceof AmazonS3Client);
        assertEquals(result.getRegion().toAWSRegion().getName(), Regions.DEFAULT_REGION.getName());
        verify(myLogger).warning("Region 'invalid-region-name' not valid. Using default region: 'us-west-2'...");
    }

    @Test
    public void testCreateClientWithProxy() throws Exception {
        final String publicKey = "AAA";
        final String privateKey = "ZZZ";
        final String region = "sa-east-1";
        final String proxyUrl = "http://localhost:3128";

        AmazonS3 result = helper.createClientWithProxy(publicKey, privateKey, region, proxyUrl);
        assertTrue(result instanceof AmazonS3Client);
        assertEquals(result.getRegion().toAWSRegion().getName(), Regions.SA_EAST_1.getName());
    }

    @Test
    public void testCreateClientWithProxyFailureWhenInvalidRegion() throws Exception {
        final String publicKey = "AAA";
        final String privateKey = "ZZZ";
        final String region = "invalid-region-name-again";
        final String proxyUrl = "http://localhost:3128";

        AmazonS3 result = helper.createClientWithProxy(publicKey, privateKey, region, proxyUrl);
        assertTrue(result instanceof AmazonS3Client);
        assertEquals(result.getRegion().toAWSRegion().getName(), Regions.DEFAULT_REGION.getName());
        verify(myLogger).warning("Region 'invalid-region-name-again' not valid. Using default region: 'us-west-2'...");
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