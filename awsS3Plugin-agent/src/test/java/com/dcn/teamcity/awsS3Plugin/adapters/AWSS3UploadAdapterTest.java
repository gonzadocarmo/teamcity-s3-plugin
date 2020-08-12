package com.dcn.teamcity.awsS3Plugin.adapters;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.transfer.*;
import jetbrains.buildServer.agent.BuildProgressLogger;
import jetbrains.buildServer.agent.impl.artifacts.ArtifactsCollection;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.util.*;

import static org.mockito.Mockito.*;

/**
 * @author <a href="mailto:gonzalo.docarmo@gmail.com">Gonzalo G. do Carmo Norte</a>
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({TransferManagerBuilder.class})
public class AWSS3UploadAdapterTest {

    private AWSS3UploadAdapter adapter;
    private AmazonS3 amazonS3Client;
    private String bucketName;
    private File source;
    private File source1;
    private File source2;
    private String httpHeaderCacheControl;
    private BuildProgressLogger loggerMock;

    private TransferManagerBuilder tmb;

    @Mock
    private TransferManager tm;

    @Mock
    private MultipleFileUpload multipleFileUpload;

    @Mock
    private Upload upload1;

    @Mock
    private Upload upload2;

    @Mock
    TransferProgress tf1;

    @Mock
    TransferProgress tf2;

    String destinationDir;
    List<ArtifactsCollection> myArtifacts;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        bucketName = "my-bucket";
        httpHeaderCacheControl = "max-age=0, no-cache, no-store";
        loggerMock = Mockito.mock(BuildProgressLogger.class);
        adapter = new AWSS3UploadAdapter().withLogger(loggerMock);
        amazonS3Client = Mockito.mock(AmazonS3.class);
        source = File.createTempFile("temp", ".txt");
        source1 = File.createTempFile("temp", ".pdf");
        source2 = File.createTempFile("temp", ".gif");
        source.deleteOnExit();
        source1.deleteOnExit();
        source2.deleteOnExit();

        destinationDir = "src/here";
        myArtifacts = createArtifactsCollections(destinationDir);

        when(tf1.getPercentTransferred()).thenReturn(10d).thenReturn(25d).thenReturn(80d).thenReturn(100d);
        when(tf2.getPercentTransferred()).thenReturn(93d).thenReturn(100d);

        doReturn("Subtransfer 1 desc").when(upload1).getDescription();
        when(upload1.isDone()).thenReturn(false).thenReturn(false).thenReturn(false).thenReturn(true);
        when(upload1.getProgress()).thenReturn(tf1);
        when(upload1.getState()).thenReturn(Transfer.TransferState.Completed);

        doReturn("Subtransfer 2 desc").when(upload2).getDescription();
        when(upload2.isDone()).thenReturn(false).thenReturn(true);
        when(upload2.getProgress()).thenReturn(tf2);
        when(upload2.getState()).thenReturn(Transfer.TransferState.Completed);

        Collection<Upload> subTransfers = new ArrayList<>();
        subTransfers.add(upload1);
        subTransfers.add(upload2);

        doReturn(subTransfers).when(multipleFileUpload).getSubTransfers();
        when(multipleFileUpload.isDone()).thenReturn(false).thenReturn(false).thenReturn(false).thenReturn(true);
        when(multipleFileUpload.getState()).thenReturn(Transfer.TransferState.Completed);

        doReturn(multipleFileUpload).when(tm).uploadFileList(anyString(), anyString(), any(File.class), ArgumentMatchers.<File>anyList(), any(ObjectMetadataProvider.class));

        PowerMockito.mockStatic(TransferManagerBuilder.class);
        tmb = PowerMockito.mock(TransferManagerBuilder.class);
        PowerMockito.when(TransferManagerBuilder.standard()).thenReturn(tmb);
        PowerMockito.when(tmb.withS3Client(any(AmazonS3.class))).thenReturn(tmb);
        PowerMockito.when(tmb.build()).thenReturn(tm);
    }

    @NotNull
    private List<ArtifactsCollection> createArtifactsCollections(String destinationDir) {
        Map<File, String> filePathMap = new HashMap<>();
        filePathMap.put(source, source.getAbsolutePath());
        filePathMap.put(source1, source1.getAbsolutePath());

        Map<File, String> filePathMap2 = new HashMap<>();
        filePathMap2.put(source2, source2.getAbsolutePath());

        ArtifactsCollection artifactCollection1 = new ArtifactsCollection(source.getAbsolutePath(), destinationDir, filePathMap);
        ArtifactsCollection artifactCollection2 = new ArtifactsCollection(source2.getAbsolutePath(), destinationDir, filePathMap2);

        List<ArtifactsCollection> myArtifacts = new ArrayList<>();
        myArtifacts.add(artifactCollection1);
        myArtifacts.add(artifactCollection2);
        return myArtifacts;
    }

    private List<ArtifactsCollection> createArtifactsCollectionsWithNoFiles(String destinationDir) {
        ArtifactsCollection artifactCollection = new ArtifactsCollection(source.getAbsolutePath(), destinationDir, new HashMap<File, String>());
        List<ArtifactsCollection> myArtifacts = new ArrayList<>();
        myArtifacts.add(artifactCollection);
        return myArtifacts;
    }

    @Test
    public void testUploadToBucketWhenFilesToUpload() throws Exception {
        adapter.uploadToBucket(bucketName, amazonS3Client, myArtifacts, httpHeaderCacheControl);

        verify(tm, times(2)).uploadFileList(eq(bucketName), anyString(), eq(source1.getParentFile()), ArgumentMatchers.<File>anyList(), any(ObjectMetadataProvider.class));

        verify(loggerMock, times(2)).message("Done");
        verify(loggerMock, times(2)).activityStarted(anyString(), eq("This is one batch of files"));
        verify(loggerMock, times(2)).activityFinished(anyString(), eq("This is one batch of files"));

        verify(loggerMock, times(5)).message("Subtransfer 1 desc");
        verify(loggerMock).message("10% completed");
        verify(loggerMock).message("25% completed");
        verify(loggerMock).message("80% completed");
        verify(loggerMock, times(2)).message("Subtransfer 1 desc - Completed");

        verify(loggerMock, times(5)).message("Subtransfer 2 desc");
        verify(loggerMock).message("93% completed");
        verify(loggerMock, times(4)).message("Subtransfer 2 desc - Completed");
    }

    @Test
    public void testUploadToBucketWhenNoFilesFromOneDirectory() throws Exception {
        List<ArtifactsCollection> noFilesOnSecondDirectory = new ArrayList<>(myArtifacts);
        noFilesOnSecondDirectory.add(new ArtifactsCollection("", "", new HashMap<File, String>()));
        adapter.uploadToBucket(bucketName, amazonS3Client, noFilesOnSecondDirectory, httpHeaderCacheControl);

        verify(tm, times(2)).uploadFileList(eq(bucketName), anyString(), eq(source1.getParentFile()), ArgumentMatchers.<File>anyList(), any(ObjectMetadataProvider.class));

        verify(loggerMock, times(2)).message("Done");
        verify(loggerMock, times(3)).activityStarted(anyString(), eq("This is one batch of files"));
        verify(loggerMock).warning("No files found to upload!");
        verify(loggerMock, times(3)).activityFinished(anyString(), eq("This is one batch of files"));

        verify(loggerMock, times(5)).message("Subtransfer 1 desc");
        verify(loggerMock).message("10% completed");
        verify(loggerMock).message("25% completed");
        verify(loggerMock).message("80% completed");
        verify(loggerMock, times(2)).message("Subtransfer 1 desc - Completed");

        verify(loggerMock, times(5)).message("Subtransfer 2 desc");
        verify(loggerMock).message("93% completed");
        verify(loggerMock, times(4)).message("Subtransfer 2 desc - Completed");
    }

    @Test
    public void testUploadToBucketWhenNoFilesAtAllToUpload() throws Exception {
        exceptionRule.expect(Exception.class);
        exceptionRule.expectMessage("No files to upload have been found!");

        adapter.uploadToBucket(bucketName, amazonS3Client, createArtifactsCollectionsWithNoFiles("somepath"), httpHeaderCacheControl);

        verify(tm, never()).uploadFileList(eq(bucketName), anyString(), any(File.class), ArgumentMatchers.<File>anyList(), any(ObjectMetadataProvider.class));

        verify(loggerMock, never()).message("Done");
        verify(loggerMock, times(2)).activityStarted(anyString(), eq("This is one batch of files"));
        verify(loggerMock, times(2)).activityFinished(anyString(), eq("This is one batch of files"));

        verify(loggerMock, never()).message("Subtransfer 1 desc");
        verify(loggerMock, never()).message("10% completed");
        verify(loggerMock, never()).message("25% completed");
        verify(loggerMock, never()).message("80% completed");
        verify(loggerMock, never()).message("Subtransfer 1 desc - Completed");

        verify(loggerMock, never()).message("Subtransfer 2 desc");
        verify(loggerMock, never()).message("93% completed");
        verify(loggerMock, never()).message("Subtransfer 2 desc - Completed");

        verify(loggerMock).warning("No files found to upload!");
    }

    @Test
    public void testUploadToBucketWhenFilesToUploadAndTransferNotCompleted() throws Exception {
        exceptionRule.expect(SdkClientException.class);
        exceptionRule.expectMessage("Error occurred while transferring data. Status is: Failed");

        when(multipleFileUpload.getState()).thenReturn(Transfer.TransferState.Failed);

        adapter.uploadToBucket(bucketName, amazonS3Client, myArtifacts, httpHeaderCacheControl);
    }
}
