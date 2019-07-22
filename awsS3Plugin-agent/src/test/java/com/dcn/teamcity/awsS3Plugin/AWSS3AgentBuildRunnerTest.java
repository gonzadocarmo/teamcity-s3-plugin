package com.dcn.teamcity.awsS3Plugin;

import com.dcn.teamcity.awsS3Plugin.adapters.AWSS3Adapter;
import jetbrains.buildServer.ExtensionHolder;
import jetbrains.buildServer.agent.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertTrue;

/**
 * Created by sg0216948 on 7/13/16.
 *
 * @author <a href="mailto:gonzalo.docarmo@gmail.com">Gonzalo G. do Carmo Norte</a>
 */
public class AWSS3AgentBuildRunnerTest {

    private AgentBuildRunner runner;
    private ExtensionHolder extensionHolderMock;
    private AWSS3Adapter awsS3Adapter;
    private AWSS3BuildProcessAdapterHelper helperMock;
    private AgentRunningBuild agentRunningBuildMock;
    private BuildRunnerContext buildRunnerContextMock;
    private BuildProgressLogger buildProgressLogger;

    @BeforeMethod
    public void setUp() throws Exception {
        extensionHolderMock = mock(ExtensionHolder.class);
        awsS3Adapter = mock(AWSS3Adapter.class);
        helperMock = mock(AWSS3BuildProcessAdapterHelper.class);

        agentRunningBuildMock = mock(AgentRunningBuild.class);
        buildRunnerContextMock = mock(BuildRunnerContext.class);
        buildProgressLogger = mock(BuildProgressLogger.class);

        when(agentRunningBuildMock.getBuildLogger()).thenReturn(buildProgressLogger);
        when(agentRunningBuildMock.getCheckoutDirectory()).thenReturn(new File("tmp.txt"));
        when(buildRunnerContextMock.getBuild()).thenReturn(agentRunningBuildMock);

        runner = new AWSS3AgentBuildRunner(extensionHolderMock, awsS3Adapter);
    }

    @Test
    public void testGetRunnerInfo() throws Exception {
        AgentBuildRunnerInfo result = runner.getRunnerInfo();

        assertTrue(result instanceof AWSS3AgentBuildRunnerInfo);
    }

    @Test
    public void testCreateBuildProcess() throws Exception {
        BuildProcess result = runner.createBuildProcess(agentRunningBuildMock, buildRunnerContextMock);

        assertTrue(result instanceof AWSS3BuildProcessAdapter);
    }
}
