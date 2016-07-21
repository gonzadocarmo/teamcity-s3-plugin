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
    private final AWSS3BuildProcessAdapterHelper adapterHelper;

    public AWSS3AgentBuildRunner(@NotNull final ExtensionHolder extensionHolder,
                                 final @NotNull AWSS3Adapter awsS3Adapter,
                                 final @NotNull AWSS3BuildProcessAdapterHelper adapterHelper) {
        myExtensionHolder = extensionHolder;
        this.awsS3Adapter = awsS3Adapter;
        this.adapterHelper = adapterHelper;
    }

    @Override
    public BuildProcess createBuildProcess(AgentRunningBuild agentRunningBuild,
                                           BuildRunnerContext buildRunnerContext) {
        return new AWSS3BuildProcessAdapter(buildRunnerContext.getBuild().getBuildLogger(),
                buildRunnerContext.getRunnerParameters(),
                agentRunningBuild.getCheckoutDirectory(),
                myExtensionHolder,
                awsS3Adapter,
                adapterHelper);
    }

    @Override
    public AgentBuildRunnerInfo getRunnerInfo() {
        return new AWSS3AgentBuildRunnerInfo();
    }
}