package com.dcn.teamcity.awsS3Plugin;

import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.serverSide.RunType;
import jetbrains.buildServer.serverSide.RunTypeRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Gonzalo do Carmo Norte on 7/9/16.
 *
 * @author <a href="mailto:gonzalo.docarmo@gmail.com">Gonzalo G. do Carmo Norte</a>
 */
public class AWSS3RunType extends RunType {

    public AWSS3RunType(final RunTypeRegistry runTypeRegistry) {
        runTypeRegistry.registerRunType(this);
    }

    @Override
    @Nullable
    public PropertiesProcessor getRunnerPropertiesProcessor() {
        return new AWSS3ParametersValidator();
    }

    @Override
    public String getEditRunnerParamsJspFilePath() {
        return "taskRunnerRunParams.jsp";
    }

    @Override
    public String getViewRunnerParamsJspFilePath() {
        return "viewTaskRunnerRunParams.jsp";
    }

    @Override
    public Map<String, String> getDefaultRunnerProperties() {
        return new HashMap();
    }

    @NotNull
    @Override
    public String getDescription() {
        return PluginConstants.RUNNER_DESCRIPTION;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return PluginConstants.RUNNER_DISPLAY_NAME;
    }

    @NotNull
    @Override
    public String getType() {
        return PluginConstants.RUNNER_TYPE;
    }

    @NotNull
    @Override
    public String describeParameters(@NotNull final Map<String, String> parameters) {
        StringBuilder result = new StringBuilder();

        final String BUCKET_NAME = parameters.get(PluginConstants.UI_PARAM_BUCKET_NAME);
        final String BUCKET_REGION = parameters.get(PluginConstants.UI_PARAM_BUCKET_REGION);
        final String CONTENT_PATHS = parameters.get(PluginConstants.UI_PARAM_CONTENT_PATHS);

        final String message = "Bucket: \"%s\" - %s: \"%s\"\n%s: %s";
        result.append(String.format(message,
                BUCKET_NAME,
                PluginConstants.UI_PARAM_BUCKET_REGION_DESCRIPTION,
                BUCKET_REGION,
                PluginConstants.UI_PARAM_CONTENT_PATHS_DESCRIPTION,
                CONTENT_PATHS
        ));

        return result.toString();
    }

    @Override
    public String toString() {
        return String.format("[AWSS3RunType] Type: %s",
                this.getType());
    }
}