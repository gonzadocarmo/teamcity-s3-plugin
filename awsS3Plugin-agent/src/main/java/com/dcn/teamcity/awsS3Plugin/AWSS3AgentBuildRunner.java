package com.dcn.teamcity.awsS3Plugin;

import com.dcn.teamcity.awsS3Plugin.adapters.AWSS3Adapter;
import jetbrains.buildServer.ExtensionHolder;
import jetbrains.buildServer.agent.*;
import org.jetbrains.annotations.NotNull;

/**
 * Created by sg0216948 on 7/10/16.
 *
 * @author <a href="mailto:gonzalo.docarmo@gmail.com">Gonzalo G. do Carmo Norte</a>
 */
public class AWSS3AgentBuildRunner implements AgentBuildRunner {

    protected final ExtensionHolder myExtensionHolder;
    private final AWSS3Adapter awsS3Adapter;

    public AWSS3AgentBuildRunner(@NotNull final ExtensionHolder extensionHolder,
                                 final @NotNull AWSS3Adapter awsS3Adapter) {
        myExtensionHolder = extensionHolder;
        this.awsS3Adapter = awsS3Adapter;
    }

    @Override
    public BuildProcess createBuildProcess(AgentRunningBuild agentRunningBuild,
                                           BuildRunnerContext buildRunnerContext) {
        BuildProgressLogger logger = buildRunnerContext.getBuild().getBuildLogger();
        return new AWSS3BuildProcessAdapter(logger,
                buildRunnerContext.getRunnerParameters(),
                agentRunningBuild.getCheckoutDirectory(),
                myExtensionHolder,
                awsS3Adapter,
                new AWSS3BuildProcessAdapterHelper().withLogger(logger));
    }

    @Override
    public AgentBuildRunnerInfo getRunnerInfo() {
        return new AWSS3AgentBuildRunnerInfo();
    }
}