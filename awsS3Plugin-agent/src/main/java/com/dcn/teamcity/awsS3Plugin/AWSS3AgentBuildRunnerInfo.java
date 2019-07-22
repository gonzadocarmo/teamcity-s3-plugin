package com.dcn.teamcity.awsS3Plugin;

import jetbrains.buildServer.agent.AgentBuildRunnerInfo;
import jetbrains.buildServer.agent.BuildAgentConfiguration;

/**
 * @author <a href="mailto:gonzalo.docarmo@gmail.com">Gonzalo G. do Carmo Norte</a>
 */
public class AWSS3AgentBuildRunnerInfo implements AgentBuildRunnerInfo {

    @Override
    public String getType() {
        return PluginConstants.RUNNER_TYPE;
    }

    @Override
    public boolean canRun(BuildAgentConfiguration buildAgentConfiguration) {
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null &&
                obj instanceof AWSS3AgentBuildRunnerInfo &&
                this.getType().equals(((AWSS3AgentBuildRunnerInfo) obj).getType());
    }

    @Override
    public int hashCode() {
        return Long.valueOf((2 * 31 + 3) * 31 + 5).hashCode();
    }

    @Override
    public String toString() {
        return String.format("[AWSS3AgentBuildRunnerInfo] Type: %s",
                this.getType());
    }
}
