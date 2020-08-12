package com.dcn.teamcity.awsS3Plugin.adapters;

import jetbrains.buildServer.agent.BuildProgressLogger;
import org.jetbrains.annotations.NotNull;

/**
 * @author <a href="mailto:gonzalo.docarmo@gmail.com">Gonzalo G. do Carmo Norte</a>
 */
public class
AWSS3Adapter {

    private final AWSS3UploadAdapter uploadAdapter;
    private final AWSS3DeleteAdapter deleteAdapter;
    private BuildProgressLogger logger;


    public AWSS3Adapter(@NotNull final AWSS3UploadAdapter uploadAdapter,
                        @NotNull final AWSS3DeleteAdapter deleteAdapter) {
        this.uploadAdapter = uploadAdapter;
        this.deleteAdapter = deleteAdapter;
    }

    public AWSS3UploadAdapter getUploadAdapter() {
        return uploadAdapter.withLogger(logger);
    }

    public AWSS3DeleteAdapter getDeleteAdapter() {
        return deleteAdapter;
    }

    public AWSS3Adapter withLogger(BuildProgressLogger logger) {
        this.logger = logger;
        return this;
    }
}
