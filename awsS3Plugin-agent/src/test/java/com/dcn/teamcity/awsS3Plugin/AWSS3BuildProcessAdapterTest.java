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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        uploadAdapterMock = mock(AWSS3UploadAdapter.class);
        deleteAdapterMock = mock(AWSS3DeleteAdapter.class);

        awsS3AdapterMock = new AWSS3Adapter(uploadAdapterMock, deleteAdapterMock);
        processAdapterHelperMock = mock(AWSS3BuildProcessAdapterHelper.class);
        when(processAdapterHelperMock.createClient(anyString(), anyString(), anyString())).thenReturn(amazonS3Mock);
        when(processAdapterHelperMock.createClientWithProxy(anyString(), anyString(), anyString(), anyString())).thenReturn(amazonS3Mock);
    }

    @Test
    public void testRunProcessNoValidProxy() throws Exception {
        final String bucketName = "someName";
        final String bucketRegion = "someRegion";
        final String uploadTaskLogBucket = uploadTaskLog + bucketName;
        final String httpProxy = "http://username@notvalid:::";

        mockBuilderCreateModel(bucketName, bucketRegion, "false", null, httpProxy);
        when(processAdapterHelperMock.createClientWithProxy(anyString(), anyString(), anyString(), anyString())).thenThrow(Exception.class);


        adapter = new AWSS3BuildProcessAdapter(loggerMock, runnerParametersMock, agentCheckoutDirectoryMock, extensionHolderMock, awsS3AdapterMock, processAdapterHelperMock);

        adapter.runProcess();

        verify(loggerMock).warning("HTTP Proxy URL not valid: 'http://username@notvalid:::'. Skipping...");

        verifyDeleteOperationNotExecuted(bucketName);

        verify(loggerMock).targetStarted(uploadTaskLogBucket);
        verify(uploadAdapterMock, never()).uploadToBucket(anyString(), any(AmazonS3.class), any(File.class), anyString(), anyString());
        verify(loggerMock).error("Failed: " + noUploadErrorLog);
        verify(loggerMock).logBuildProblem(any(BuildProblemData.class));
        verify(processAdapterHelperMock).createBuildProblemData(bucketName, noUploadErrorLog);
        verify(loggerMock).targetFinished(uploadTaskLogBucket);
    }

    @Test
    public void testRunProcessNoDeleteNoFilesToUpload() throws Exception {
        final String bucketName = "someName";
        final String bucketRegion = "someRegion";
        final String uploadTaskLogBucket = uploadTaskLog + bucketName;

        mockBuilderCreateModel(bucketName, bucketRegion, "false", null, null);


        adapter = new AWSS3BuildProcessAdapter(loggerMock, runnerParametersMock, agentCheckoutDirectoryMock, extensionHolderMock, awsS3AdapterMock, processAdapterHelperMock);

        adapter.runProcess();

        verifyDeleteOperationNotExecuted(bucketName);

        verify(loggerMock).targetStarted(uploadTaskLogBucket);
        verify(uploadAdapterMock, never()).uploadToBucket(anyString(), any(AmazonS3.class), any(File.class), anyString(), anyString());
        verify(loggerMock).error("Failed: " + noUploadErrorLog);
        verify(loggerMock).logBuildProblem(any(BuildProblemData.class));
        verify(processAdapterHelperMock).createBuildProblemData(bucketName, noUploadErrorLog);
        verify(loggerMock).targetFinished(uploadTaskLogBucket);
    }

    @NotNull
    private void mockBuilderCreateModel(String bucketName, String bucketRegion, String needToEmptyBucket, String sourcePaths, String httpProxy) {
        final String publicKey = "";
        final String privateKey = publicKey;

        AgentRunnerBuildParametersModel model = new AgentRunnerBuildParametersModel(bucketName, bucketRegion, publicKey, privateKey, Boolean.valueOf(needToEmptyBucket), sourcePaths, null, httpProxy);
        when(processAdapterHelperMock.createModel(anyMap())).thenReturn(model);
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

        runnerParametersMock.put(PluginConstants.UI_PARAM_BUCKET_NAME, bucketName);
        runnerParametersMock.put(PluginConstants.UI_PARAM_CONTENT_PATHS, desiredArtifactPaths);

        Map<File, String> filePathMap = new HashMap<>();
        filePathMap.put(file1, sourcePath);
        filePathMap.put(file2, sourcePath);
        filePathMap.put(file3, sourcePath);

        ArtifactsCollection artifactsCollection = new ArtifactsCollection(sourcePath, destinationPath, filePathMap);

        List<ArtifactsCollection> artifacts = new ArrayList<>();
        artifacts.add(artifactsCollection);

        when(processAdapterHelperMock.getArtifactsCollections(desiredArtifactPaths, extensionHolderMock, agentCheckoutDirectoryMock)).thenReturn(artifacts);
        mockBuilderCreateModel(bucketName, bucketRegion, "false", desiredArtifactPaths, null);


        adapter = new AWSS3BuildProcessAdapter(loggerMock, runnerParametersMock, agentCheckoutDirectoryMock, extensionHolderMock, awsS3AdapterMock, processAdapterHelperMock);
        adapter.runProcess();

        verifyDeleteOperationNotExecuted(bucketName);

        verify(loggerMock).targetStarted(uploadTaskLogBucket);

        verify(uploadAdapterMock).uploadToBucket(eq(bucketName), any(AmazonS3.class), eq(file1), eq(sourcePath), anyString());
        verify(loggerMock).message("Transferring [" + file1.getAbsolutePath() + "] to [" + sourcePath + "]");
        verify(loggerMock).message("done transferring [" + file1.getPath() + "]");

        verify(uploadAdapterMock).uploadToBucket(eq(bucketName), any(AmazonS3.class), eq(file2), eq(sourcePath), anyString());
        verify(loggerMock).message("Transferring [" + file2.getAbsolutePath() + "] to [" + sourcePath + "]");
        verify(loggerMock).message("done transferring [" + file2.getPath() + "]");

        verify(uploadAdapterMock).uploadToBucket(eq(bucketName), any(AmazonS3.class), eq(file3), eq(sourcePath), anyString());
        verify(loggerMock).message("Transferring [" + file3.getAbsolutePath() + "] to [" + sourcePath + "]");
        verify(loggerMock).message("done transferring [" + file3.getPath() + "]");

        verify(loggerMock).message("Uploaded [" + filePathMap.size() + "] files for [" + artifactsCollection.getSourcePath() + "] pattern");
        verify(loggerMock).message(taskDoneLog);

        verify(loggerMock, never()).error("Failed: " + noUploadErrorLog);
        verify(loggerMock, never()).logBuildProblem(any(BuildProblemData.class));

        verify(loggerMock).targetFinished(uploadTaskLogBucket);
    }

    @Test
    public void testRunProcessNoDeleteSomeFilesToUploadButNotMatchingCriteria() throws Exception {
        final String bucketName = "someName";
        final String bucketRegion = "someRegion";
        final String uploadTaskLogBucket = uploadTaskLog + bucketName;
        final String desiredArtifactPaths = "examples/subfolder";
        final String sourcePath = "src";
        final String destinationPath = sourcePath;

        runnerParametersMock.put(PluginConstants.UI_PARAM_BUCKET_NAME, bucketName);
        runnerParametersMock.put(PluginConstants.UI_PARAM_CONTENT_PATHS, desiredArtifactPaths);

        Map<File, String> filePathMap = new HashMap<>();

        ArtifactsCollection artifactsCollection = new ArtifactsCollection(sourcePath, destinationPath, filePathMap);

        List<ArtifactsCollection> artifacts = new ArrayList<>();
        artifacts.add(artifactsCollection);

        when(processAdapterHelperMock.getArtifactsCollections(desiredArtifactPaths, extensionHolderMock, agentCheckoutDirectoryMock)).thenReturn(artifacts);
        mockBuilderCreateModel(bucketName, bucketRegion, "false", desiredArtifactPaths, null);


        adapter = new AWSS3BuildProcessAdapter(loggerMock, runnerParametersMock, agentCheckoutDirectoryMock, extensionHolderMock, awsS3AdapterMock, processAdapterHelperMock);
        adapter.runProcess();

        verifyDeleteOperationNotExecuted(bucketName);

        verify(loggerMock).targetStarted(uploadTaskLogBucket);
        verify(uploadAdapterMock, never()).uploadToBucket(anyString(), any(AmazonS3.class), any(File.class), anyString(), anyString());
        verify(loggerMock, never()).message(taskDoneLog);
        verify(loggerMock).error("Failed: " + noUploadErrorLog);
        verify(loggerMock).logBuildProblem(any(BuildProblemData.class));
        verify(loggerMock).targetFinished(uploadTaskLogBucket);
    }

    @Test
    public void testRunProcessDeleteFiles() throws Exception {
        final String bucketName = "someBucket";
        final String bucketRegion = "someRegion";
        mockBuilderCreateModel(bucketName, bucketRegion, "true", "", null);

        adapter = new AWSS3BuildProcessAdapter(loggerMock, runnerParametersMock, agentCheckoutDirectoryMock, extensionHolderMock, awsS3AdapterMock, processAdapterHelperMock);
        adapter.runProcess();

        verifyDeleteOperationExecuted(bucketName);
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
        verify(loggerMock).message(taskDoneLog);
        verify(loggerMock).targetFinished(deleteTaskLog);
        verify(deleteAdapterMock).deleteAllContentFromBucket(eq(bucketName), any(AmazonS3.class));
    }

}
