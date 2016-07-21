package com.dcn.teamcity.awsS3Plugin;

import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.serverSide.RunTypeRegistry;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Created by sg0216948 on 7/9/16.
 *
 * @author <a href="mailto:gonzalo.docarmo@gmail.com">Gonzalo G. do Carmo Norte</a>
 */
public class AWSS3RunTypeTest {

    private AWSS3RunType instance = null;

    @BeforeMethod
    public void setUp() throws Exception {
        RunTypeRegistry registryMock = Mockito.mock(RunTypeRegistry.class);
        instance = new AWSS3RunType(registryMock);
    }

    @Test
    public void testGetRunnerPropertiesProcessor() throws Exception {
        PropertiesProcessor result = instance.getRunnerPropertiesProcessor();
        assertTrue(result instanceof AWSS3ParametersValidator);
    }

    @Test
    public void testGetEditRunnerParamsJspFilePath() throws Exception {
        final String result = instance.getEditRunnerParamsJspFilePath();
        assertEquals(result, "taskRunnerRunParams.jsp");
    }

    @Test
    public void testGetViewRunnerParamsJspFilePath() throws Exception {
        final String result = instance.getViewRunnerParamsJspFilePath();
        assertEquals(result, "viewTaskRunnerRunParams.jsp");
    }

    @Test
    public void testGetDefaultRunnerProperties() throws Exception {
        final Map<String, String> result = instance.getDefaultRunnerProperties();
        assertEquals(result.size(), 0);
    }

    @Test
    public void testGetDescription() throws Exception {
        final String result = instance.getDescription();
        assertEquals(result, PluginConstants.RUNNER_DESCRIPTION);
    }

    @Test
    public void testGetDisplayName() throws Exception {
        final String result = instance.getDisplayName();
        assertEquals(result, PluginConstants.RUNNER_DISPLAY_NAME);
    }

    @Test
    public void testGetType() throws Exception {
        final String result = instance.getType();
        assertEquals(result, PluginConstants.RUNNER_TYPE);
    }

    @Test
    public void testDescribeParameters() throws Exception {
        final String BUCKET_NAME = "myBucketName";
        final String BUCKET_REGION = "my-bucket-region-9";
        final String KEY_PUB = "myPublicKey";
        final String KEY_PRIV = "myPrivateKey";
        final String CONTENT_PATHS = "src/index.html,src/assets/*";

        Map<String, String> parameters = new HashMap<>();
        parameters.put(PluginConstants.UI_PARAM_BUCKET_NAME, BUCKET_NAME);
        parameters.put(PluginConstants.UI_PARAM_BUCKET_REGION, BUCKET_REGION);
        parameters.put(PluginConstants.UI_PARAM_CREDENTIALS_PUB_KEY, KEY_PUB);
        parameters.put(PluginConstants.UI_PARAM_CREDENTIALS_PRIVATE_KEY, KEY_PRIV);
        parameters.put(PluginConstants.UI_PARAM_CONTENT_PATHS, CONTENT_PATHS);

        final String result = instance.describeParameters(parameters);

        assertEquals(result, "Bucket: \"" + BUCKET_NAME + "\" - Region: \"" + BUCKET_REGION + "\"\nArtifacts path: " + CONTENT_PATHS);
    }

}