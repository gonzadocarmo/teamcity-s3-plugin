package com.dcn.teamcity.awsS3Plugin;

import com.amazonaws.services.s3.AmazonS3;
import com.dcn.teamcity.awsS3Plugin.adapters.AWSS3Adapter;
import com.dcn.teamcity.awsS3Plugin.adapters.AWSS3DeleteAdapter;
import com.dcn.teamcity.awsS3Plugin.adapters.AWSS3UploadAdapter;
import jetbrains.buildServer.BuildProblemData;
import jetbrains.buildServer.ExtensionHolder;
import jetbrains.buildServer.agent.BuildProgressLogger;
import jetbrains.buildServer.agent.impl.artifacts.ArtifactsCollection;
import org.jetbrains.annotations.NotNull;
import org.mockito.ArgumentMatchers;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.util.*;

import static org.mockito.Mockito.*;

/**
 * @author <a href="mailto:gonzalo.docarmo@gmail.com">Gonzalo G. do Carmo Norte</a>
 */
public class AWSS3BuildProcessAdapterTest {

    private AmazonS3 amazonS3Mock;
    private AWSS3BuildProcessAdapter adapter;
    private BuildProgressLogger loggerMock;
    private Map<String, String> runnerParametersMock;
    private File agentCheckoutDirectoryMock;
    private ExtensionHolder extensionHolderMock;
    private AWSS3Adapter awsS3AdapterMock;
    private AWSS3BuildProcessAdapterHelper processAdapterHelperMock;
    private AWSS3UploadAdapter uploadAdapterMock;
    private AWSS3DeleteAdapter deleteAdapterMock;
    private String uploadTaskLog;
    private String noUploadErrorLog;
    private String taskDoneLog;

    @BeforeMethod
    public void setUp() throws Exception {
        uploadTaskLog = "Upload to S3 bucket: ";
        noUploadErrorLog = "No files to upload have been found!";
        taskDoneLog = "Done";

        amazonS3Mock = mock(AmazonS3.class);
        loggerMock = mock(BuildProgressLogger.class);
        runnerParametersMock = new HashMap<>();
        agentCheckoutDirectoryMock = mock(File.class);
        extensionHolderMock = mock(ExtensionHolder.class);

        processAdapterHelperMock = mock(AWSS3BuildProcessAdapterHelper.class);
        when(processAdapterHelperMock.createClient(anyString(), anyString(), anyString())).thenReturn(amazonS3Mock);
        when(processAdapterHelperMock.createClientWithProxy(anyString(), anyString(), anyString(), anyString())).thenReturn(amazonS3Mock);
        when(processAdapterHelperMock.createBuildProblemData(anyString(), anyString())).thenReturn(BuildProblemData.createBuildProblem("some", "some", "some"));
    }

    @Test
    public void testRunProcessNoDeleteNoFilesToUpload() throws Exception {
        final String bucketName = "someName";
        final String bucketRegion = "someRegion";
        final String uploadTaskLogBucket = uploadTaskLog + bucketName;
        final String sourcePath = "src";
        final String destinationPath = sourcePath;
        final String desiredArtifactPaths = sourcePath;
        final File file1 = new File("src/file1.html");

        uploadAdapterMock = spy(AWSS3UploadAdapter.class);
        doReturn(uploadAdapterMock).when(uploadAdapterMock).withLogger(any(BuildProgressLogger.class));
        doThrow(new Exception("No files to upload have been found!")).when(uploadAdapterMock).uploadToBucket(anyString(), any(AmazonS3.class), ArgumentMatchers.<ArtifactsCollection>anyList(), nullable(String.class));
        deleteAdapterMock = mock(AWSS3DeleteAdapter.class);
        awsS3AdapterMock = new AWSS3Adapter(uploadAdapterMock, deleteAdapterMock).withLogger(loggerMock);
        mockGetArtifacts(desiredArtifactPaths, sourcePath, destinationPath, file1, file1, file1);
        mockBuilderCreateModel(bucketName, bucketRegion, "false", null, null);

        adapter = new AWSS3BuildProcessAdapter(loggerMock, runnerParametersMock, agentCheckoutDirectoryMock, extensionHolderMock, awsS3AdapterMock, processAdapterHelperMock);

        adapter.runProcess();

        verifyDeleteOperationNotExecuted(bucketName);
        verify(uploadAdapterMock).uploadToBucket(bucketName, amazonS3Mock, Collections.<ArtifactsCollection>emptyList(), null);
        verify(loggerMock).targetStarted(uploadTaskLogBucket);
        verify(loggerMock).logBuildProblem(any(BuildProblemData.class));
        verify(processAdapterHelperMock).createBuildProblemData(bucketName, noUploadErrorLog);
        verify(loggerMock, never()).targetFinished("Upload to S3 bucket: " + bucketName);
    }

    @Test
    public void testRunProcessNoDeleteSomeFilesToUpload() throws Exception {
        final String bucketName = "someName";
        final String bucketRegion = "someRegion";
        final String uploadTaskLogBucket = uploadTaskLog + bucketName;
        final String desiredArtifactPaths = "src,examples/subfolder";
        final String sourcePath = "src";
        final String destinationPath = sourcePath;
        final File file1 = new File("src/file1.html");
        final File file2 = new File("src/file2.html");
        final File file3 = new File("src/file3.html");

        uploadAdapterMock = spy(AWSS3UploadAdapter.class);
        doReturn(uploadAdapterMock).when(uploadAdapterMock).withLogger(any(BuildProgressLogger.class));
        doNothing().when(uploadAdapterMock).uploadToBucket(anyString(), any(AmazonS3.class), ArgumentMatchers.<ArtifactsCollection>anyList(), nullable(String.class));
        deleteAdapterMock = mock(AWSS3DeleteAdapter.class);
        awsS3AdapterMock = new AWSS3Adapter(uploadAdapterMock, deleteAdapterMock).withLogger(loggerMock);

        runnerParametersMock.put(PluginConstants.UI_PARAM_BUCKET_NAME, bucketName);
        runnerParametersMock.put(PluginConstants.UI_PARAM_CONTENT_PATHS, desiredArtifactPaths);

        List<ArtifactsCollection> artifacts = mockGetArtifacts(desiredArtifactPaths, sourcePath, destinationPath, file1, file2, file3);

        mockBuilderCreateModel(bucketName, bucketRegion, "false", desiredArtifactPaths, null);

        adapter = new AWSS3BuildProcessAdapter(loggerMock, runnerParametersMock, agentCheckoutDirectoryMock, extensionHolderMock, awsS3AdapterMock, processAdapterHelperMock);

        adapter.runProcess();

        verifyDeleteOperationNotExecuted(bucketName);

        verify(loggerMock).targetStarted(uploadTaskLogBucket);
        verify(uploadAdapterMock).uploadToBucket(bucketName, amazonS3Mock, artifacts, null);
        verify(processAdapterHelperMock, never()).createBuildProblemData(bucketName, noUploadErrorLog);
        verify(loggerMock).message(taskDoneLog);
        verify(loggerMock).targetFinished(uploadTaskLogBucket);
    }

    @Test
    public void testRunProcessDeleteNoFilesToUpload() throws Exception {
        final String bucketName = "someName";
        final String bucketRegion = "someRegion";
        final String uploadTaskLogBucket = uploadTaskLog + bucketName;
        final String sourcePath = "src";
        final String destinationPath = sourcePath;
        final String desiredArtifactPaths = sourcePath;
        final File file1 = new File("src/file1.html");

        uploadAdapterMock = spy(AWSS3UploadAdapter.class);
        doReturn(uploadAdapterMock).when(uploadAdapterMock).withLogger(any(BuildProgressLogger.class));
        doThrow(new Exception("No files to upload have been found!")).when(uploadAdapterMock).uploadToBucket(anyString(), any(AmazonS3.class), ArgumentMatchers.<ArtifactsCollection>anyList(), nullable(String.class));
        deleteAdapterMock = mock(AWSS3DeleteAdapter.class);
        awsS3AdapterMock = new AWSS3Adapter(uploadAdapterMock, deleteAdapterMock).withLogger(loggerMock);
        mockGetArtifacts(desiredArtifactPaths, sourcePath, destinationPath, file1, file1, file1);
        mockBuilderCreateModel(bucketName, bucketRegion, "true", null, null);

        adapter = new AWSS3BuildProcessAdapter(loggerMock, runnerParametersMock, agentCheckoutDirectoryMock, extensionHolderMock, awsS3AdapterMock, processAdapterHelperMock);

        adapter.runProcess();

        verifyDeleteOperationExecuted(bucketName);

        verify(uploadAdapterMock).uploadToBucket(bucketName, amazonS3Mock, Collections.<ArtifactsCollection>emptyList(), null);
        verify(loggerMock).targetStarted(uploadTaskLogBucket);
        verify(loggerMock).logBuildProblem(any(BuildProblemData.class));
        verify(processAdapterHelperMock).createBuildProblemData(bucketName, noUploadErrorLog);
        verify(loggerMock, never()).targetFinished("Upload to S3 bucket: " + bucketName);
    }

    @Test
    public void testRunProcessDeleteSomeFilesToUpload() throws Exception {
        final String bucketName = "someName";
        final String bucketRegion = "someRegion";
        final String uploadTaskLogBucket = uploadTaskLog + bucketName;
        final String desiredArtifactPaths = "src,examples/subfolder";
        final String sourcePath = "src";
        final String destinationPath = sourcePath;
        final File file1 = new File("src/file1.html");
        final File file2 = new File("src/file2.html");
        final File file3 = new File("src/file3.html");

        uploadAdapterMock = spy(AWSS3UploadAdapter.class);
        doReturn(uploadAdapterMock).when(uploadAdapterMock).withLogger(any(BuildProgressLogger.class));
        doNothing().when(uploadAdapterMock).uploadToBucket(anyString(), any(AmazonS3.class), ArgumentMatchers.<ArtifactsCollection>anyList(), nullable(String.class));
        deleteAdapterMock = mock(AWSS3DeleteAdapter.class);
        awsS3AdapterMock = new AWSS3Adapter(uploadAdapterMock, deleteAdapterMock).withLogger(loggerMock);

        runnerParametersMock.put(PluginConstants.UI_PARAM_BUCKET_NAME, bucketName);
        runnerParametersMock.put(PluginConstants.UI_PARAM_CONTENT_PATHS, desiredArtifactPaths);

        List<ArtifactsCollection> artifacts = mockGetArtifacts(desiredArtifactPaths, sourcePath, destinationPath, file1, file2, file3);

        mockBuilderCreateModel(bucketName, bucketRegion, "true", desiredArtifactPaths, null);
        adapter = new AWSS3BuildProcessAdapter(loggerMock, runnerParametersMock, agentCheckoutDirectoryMock, extensionHolderMock, awsS3AdapterMock, processAdapterHelperMock);

        adapter.runProcess();

        verifyDeleteOperationExecuted(bucketName);

        verify(loggerMock).targetStarted(uploadTaskLogBucket);
        verify(uploadAdapterMock).uploadToBucket(bucketName, amazonS3Mock, artifacts, null);
        verify(processAdapterHelperMock, never()).createBuildProblemData(bucketName, noUploadErrorLog);
        verify(loggerMock).targetFinished(uploadTaskLogBucket);
    }

    private void verifyDeleteOperationNotExecuted(String bucketName) {
        final String deleteTaskLog = "Empty S3 bucket: " + bucketName;
        verify(loggerMock).targetStarted(deleteTaskLog);
        verify(loggerMock).message("Option not set. Skipping...");
        verify(loggerMock).targetFinished(deleteTaskLog);
        verify(deleteAdapterMock, never()).deleteAllContentFromBucket(anyString(), any(AmazonS3.class));
    }

    private void verifyDeleteOperationExecuted(String bucketName) {
        final String deleteTaskLog = "Empty S3 bucket: " + bucketName;
        verify(loggerMock).targetStarted(deleteTaskLog);
        verify(loggerMock, atMost(2)).message(taskDoneLog);
        verify(loggerMock).targetFinished(deleteTaskLog);
        verify(deleteAdapterMock).deleteAllContentFromBucket(eq(bucketName), any(AmazonS3.class));
    }

    @NotNull
    private void mockBuilderCreateModel(String bucketName, String bucketRegion, String needToEmptyBucket, String sourcePaths, String httpProxy) {
        final String publicKey = "";
        final String privateKey = publicKey;

        AgentRunnerBuildParametersModel model = new AgentRunnerBuildParametersModel(bucketName, bucketRegion, publicKey, privateKey, Boolean.valueOf(needToEmptyBucket), sourcePaths, null, httpProxy);
        when(processAdapterHelperMock.createModel(ArgumentMatchers.<String, String>anyMap())).thenReturn(model);
    }

    @NotNull
    private List<ArtifactsCollection> mockGetArtifacts(String desiredArtifactPaths, String sourcePath, String destinationPath, File file1, File file2, File file3) {
        Map<File, String> filePathMap = new HashMap<>();
        filePathMap.put(file1, sourcePath);
        filePathMap.put(file2, sourcePath);
        filePathMap.put(file3, sourcePath);

        ArtifactsCollection artifactsCollection = new ArtifactsCollection(sourcePath, destinationPath, filePathMap);

        List<ArtifactsCollection> artifacts = new ArrayList<>();
        artifacts.add(artifactsCollection);

        when(processAdapterHelperMock.getArtifactsCollections(desiredArtifactPaths, extensionHolderMock, agentCheckoutDirectoryMock)).thenReturn(artifacts);
        return artifacts;
    }

}
