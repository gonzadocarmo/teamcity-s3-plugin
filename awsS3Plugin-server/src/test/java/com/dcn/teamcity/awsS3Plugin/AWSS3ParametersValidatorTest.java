package com.dcn.teamcity.awsS3Plugin;

import jetbrains.buildServer.serverSide.InvalidProperty;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;


/**
 * @author <a href="mailto:gonzalo.docarmo@gmail.com">Gonzalo G. do Carmo Norte</a>
 */
public class AWSS3ParametersValidatorTest {

    private AWSS3ParametersValidator validator = null;

    @BeforeMethod
    public void setUp() throws Exception {
        validator = new AWSS3ParametersValidator();
    }

    @Test
    public void testProcessWhenAllRequiredValuesMissing() throws Exception {
        Map<String, String> input = new HashMap();

        Collection<InvalidProperty> result = validator.process(input);

        assertEquals(result.size(), 5);

        ArrayList<InvalidProperty> result_array = (ArrayList<InvalidProperty>) result;
        assertEquals(result_array.get(0).getPropertyName(), PluginConstants.UI_PARAM_BUCKET_NAME);
        assertEquals(result_array.get(0).getInvalidReason(), String.format(validator.VALIDATOR_MISSING_VALUE_ERROR, PluginConstants.UI_PARAM_BUCKET_NAME_DESCRIPTION));

        assertEquals(result_array.get(1).getPropertyName(), PluginConstants.UI_PARAM_BUCKET_REGION);
        assertEquals(result_array.get(1).getInvalidReason(), String.format(validator.VALIDATOR_MISSING_VALUE_ERROR, PluginConstants.UI_PARAM_BUCKET_REGION_DESCRIPTION));

        assertEquals(result_array.get(2).getPropertyName(), PluginConstants.UI_PARAM_CREDENTIALS_PUB_KEY);
        assertEquals(result_array.get(2).getInvalidReason(), String.format(validator.VALIDATOR_MISSING_VALUE_ERROR, PluginConstants.UI_PARAM_CREDENTIALS_PUB_KEY_DESCRIPTION));

        assertEquals(result_array.get(3).getPropertyName(), PluginConstants.UI_PARAM_CREDENTIALS_PRIVATE_KEY);
        assertEquals(result_array.get(3).getInvalidReason(), String.format(validator.VALIDATOR_MISSING_VALUE_ERROR, PluginConstants.UI_PARAM_CREDENTIALS_PRIVATE_KEY_DESCRIPTION));

        assertEquals(result_array.get(4).getPropertyName(), PluginConstants.UI_PARAM_CONTENT_PATHS);
        assertEquals(result_array.get(4).getInvalidReason(), String.format(validator.VALIDATOR_MISSING_VALUE_ERROR, PluginConstants.UI_PARAM_CONTENT_PATHS_DESCRIPTION));
    }

    @Test
    public void testProcessWhenSomeRequiredValuesMissing() throws Exception {
        Map<String, String> input = new HashMap();
        input.put(PluginConstants.UI_PARAM_BUCKET_NAME, "someValue");
        input.put(PluginConstants.UI_PARAM_CREDENTIALS_PUB_KEY, "someValue");

        Collection<InvalidProperty> result = validator.process(input);

        assertEquals(result.size(), 3);

        ArrayList<InvalidProperty> result_array = (ArrayList<InvalidProperty>) result;
        assertEquals(result_array.get(0).getPropertyName(), PluginConstants.UI_PARAM_BUCKET_REGION);
        assertEquals(result_array.get(1).getPropertyName(), PluginConstants.UI_PARAM_CREDENTIALS_PRIVATE_KEY);
        assertEquals(result_array.get(2).getPropertyName(), PluginConstants.UI_PARAM_CONTENT_PATHS);
    }

    @Test
    public void testProcessWhenAllRequiredValuesProvided() throws Exception {
        Map<String, String> input = new HashMap();
        input.put(PluginConstants.UI_PARAM_BUCKET_NAME, "someValue");
        input.put(PluginConstants.UI_PARAM_BUCKET_REGION, "someValue");

        input.put(PluginConstants.UI_PARAM_CREDENTIALS_PUB_KEY, "someValue");
        input.put(PluginConstants.UI_PARAM_CREDENTIALS_PRIVATE_KEY, "someValue");

        input.put(PluginConstants.UI_PARAM_CONTENT_PATHS, "someValue");

        Collection<InvalidProperty> result = validator.process(input);

        assertEquals(result.size(), 0);
    }

    @Test
    public void testIsValidURL() throws Exception {
        final String input1 = "http://localhost:3128";
        final String input2 = "https://www.domain.com:3128";
        final String input3 = "http://username:password@www.domain.com:3128";
        final String input4 = "https://domain\\username:password@www.domain.com:7001";

        assertTrue(validator.isValidUrl(input1));
        assertTrue(validator.isValidUrl(input2));
        assertTrue(validator.isValidUrl(input3));
        assertTrue(validator.isValidUrl(input4));
    }

    @Test
    public void testIsValidURLWrongFormat() throws Exception {
        final String input1 = "://something";
        final String input2 = "something";
        final String input3 = "www.domain.com";

        assertFalse(validator.isValidUrl(input1));
        assertFalse(validator.isValidUrl(input2));
        assertFalse(validator.isValidUrl(input3));
    }
}
