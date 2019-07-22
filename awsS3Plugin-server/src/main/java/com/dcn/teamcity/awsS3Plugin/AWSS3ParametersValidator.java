package com.dcn.teamcity.awsS3Plugin;

import com.intellij.openapi.util.text.StringUtil;
import jetbrains.buildServer.serverSide.InvalidProperty;
import jetbrains.buildServer.serverSide.PropertiesProcessor;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * @author <a href="mailto:gonzalo.docarmo@gmail.com">Gonzalo G. do Carmo Norte</a>
 */
public class AWSS3ParametersValidator implements PropertiesProcessor {

    public final String VALIDATOR_MISSING_VALUE_ERROR = "%s must be specified!";
    public final String VALIDATOR_INCORRECT_VALUE_ERROR = "Format is not valid!";

    public Collection<InvalidProperty> process(final Map<String, String> properties) {
        final Collection<InvalidProperty> ret = new ArrayList();

        if (StringUtil.isEmpty(properties.get(PluginConstants.UI_PARAM_BUCKET_NAME))) {
            ret.add(new InvalidProperty(PluginConstants.UI_PARAM_BUCKET_NAME,
                    String.format(VALIDATOR_MISSING_VALUE_ERROR, PluginConstants.UI_PARAM_BUCKET_NAME_DESCRIPTION)));
        }

        if (StringUtil.isEmpty(properties.get(PluginConstants.UI_PARAM_BUCKET_REGION))) {
            ret.add(new InvalidProperty(PluginConstants.UI_PARAM_BUCKET_REGION,
                    String.format(VALIDATOR_MISSING_VALUE_ERROR, PluginConstants.UI_PARAM_BUCKET_REGION_DESCRIPTION)));
        }

        if (StringUtil.isEmpty(properties.get(PluginConstants.UI_PARAM_CREDENTIALS_PUB_KEY))) {
            ret.add(new InvalidProperty(PluginConstants.UI_PARAM_CREDENTIALS_PUB_KEY,
                    String.format(VALIDATOR_MISSING_VALUE_ERROR, PluginConstants.UI_PARAM_CREDENTIALS_PUB_KEY_DESCRIPTION)));
        }

        if (StringUtil.isEmpty(properties.get(PluginConstants.UI_PARAM_CREDENTIALS_PRIVATE_KEY))) {
            ret.add(new InvalidProperty(PluginConstants.UI_PARAM_CREDENTIALS_PRIVATE_KEY,
                    String.format(VALIDATOR_MISSING_VALUE_ERROR, PluginConstants.UI_PARAM_CREDENTIALS_PRIVATE_KEY_DESCRIPTION)));
        }

        if (StringUtil.isEmpty(properties.get(PluginConstants.UI_PARAM_CONTENT_PATHS))) {
            ret.add(new InvalidProperty(PluginConstants.UI_PARAM_CONTENT_PATHS,
                    String.format(VALIDATOR_MISSING_VALUE_ERROR, PluginConstants.UI_PARAM_CONTENT_PATHS_DESCRIPTION)));
        }

        if (!StringUtil.isEmpty(properties.get(PluginConstants.UI_PARAM_HTTP_PROXY)) &&
                !isValidUrl(properties.get(PluginConstants.UI_PARAM_HTTP_PROXY))) {
            ret.add(new InvalidProperty(PluginConstants.UI_PARAM_HTTP_PROXY,
                    VALIDATOR_INCORRECT_VALUE_ERROR));
        }
        return ret;
    }

    public boolean isValidUrl(String url) {
        try {
            new URL(url);
        } catch (MalformedURLException mue) {
            return false;
        }
        return true;
    }
}
