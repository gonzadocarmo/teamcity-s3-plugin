package com.dcn.teamcity.awsS3Plugin;

import jetbrains.buildServer.agent.BuildAgentConfiguration;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author <a href="mailto:gonzalo.docarmo@gmail.com">Gonzalo G. do Carmo Norte</a>
 */
public class AWSS3AgentBuildRunnerInfoTest {

    private AWSS3AgentBuildRunnerInfo agentBuildRunnerInfo;
    private BuildAgentConfiguration buildAgentConfiguration;

    @BeforeMethod
    public void setUp() throws Exception {
        buildAgentConfiguration = mock(BuildAgentConfiguration.class);

        agentBuildRunnerInfo = new AWSS3AgentBuildRunnerInfo();
    }

    @Test
    public void tesGetType() throws Exception {
        assertEquals(agentBuildRunnerInfo.getType(), PluginConstants.RUNNER_TYPE);
    }

    @Test
    public void tesCanRun() throws Exception {
        assertTrue(agentBuildRunnerInfo.canRun(buildAgentConfiguration));
    }
}
