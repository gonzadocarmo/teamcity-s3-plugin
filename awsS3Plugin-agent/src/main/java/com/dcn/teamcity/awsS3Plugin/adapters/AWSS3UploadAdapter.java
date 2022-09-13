package com.dcn.teamcity.awsS3Plugin.adapters;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.transfer.*;
import jetbrains.buildServer.agent.BuildProgressLogger;
import jetbrains.buildServer.agent.impl.artifacts.ArtifactsCollection;

import java.io.File;
import java.util.*;

/**
 * @author <a href="mailto:gonzalo.docarmo@gmail.com">Gonzalo G. do Carmo Norte</a>
 */
public class AWSS3UploadAdapter {

    private BuildProgressLogger myLogger;

    public AWSS3UploadAdapter withLogger(BuildProgressLogger logger) {
        this.myLogger = logger;
        return this;
    }

    public void uploadToBucket(String bucketName, AmazonS3 s3Client, List<ArtifactsCollection> myArtifacts, final String cacheControlHeader) throws Exception {
        ObjectMetadataProvider omp = new ObjectMetadataProvider() {
            @Override
            public void provideObjectMetadata(File file, ObjectMetadata objectMetadata) {
                if (cacheControlHeader != null) {
                    objectMetadata.setCacheControl(cacheControlHeader);
                }
            }
        };
        TransferManager transferManager = TransferManagerBuilder.standard()
                .withS3Client(s3Client)
                .build();
        int totalCount = 0;
        int batchCount = 0;

        for (ArtifactsCollection artifactsCollection : myArtifacts) {
            final int artifactsCollectionSize = artifactsCollection.getFilePathMap().size();
            final String activityText = String.format("Batch #%d - pattern [\"%s\"] (%d files)", ++batchCount, artifactsCollection.getSourcePath(), artifactsCollectionSize);

            myLogger.activityStarted(activityText, "This is one batch of files");
            if (artifactsCollectionSize > 0) {
                totalCount += artifactsCollectionSize;
                uploadFiles(bucketName, artifactsCollection, omp, transferManager);
            } else {
                myLogger.warning("No files found to upload!");
            }
            myLogger.activityFinished(activityText, "This is one batch of files");
        }

        transferManager.shutdownNow();

        if (totalCount == 0) {
            throw new Exception("No files to upload have been found!");
        }
    }

    private void uploadFiles(String bucketName, ArtifactsCollection artifactsCollection, ObjectMetadataProvider omp, TransferManager tm) throws InterruptedException, SdkClientException {
        String virtualDirectoryKeyPrefix = artifactsCollection.getTargetPath();
        Set<File> filesSet = artifactsCollection.getFilePathMap().keySet();
        ArrayList<File> filesList = new ArrayList<>(filesSet);
        File commonDirectory = findCommonDirectory(filesList);

        MultipleFileUpload xfer = tm.uploadFileList(bucketName, virtualDirectoryKeyPrefix, commonDirectory, filesList, omp);
        showMultiUploadProgress(xfer);
        myLogger.message("Done");
    }

    private File findCommonDirectory(List<File> files) {
        if (files.size() == 0) throw new IllegalArgumentException("Empty");
        String prefix = files.get(0).getParentFile().getAbsolutePath();
        for (int i = 1; i < files.size(); i++)
            while (files.get(i).getParentFile().getAbsolutePath().indexOf(prefix) != 0) {
                prefix = prefix.substring(0, prefix.length() - 1);
                if (prefix.isEmpty()) throw new IllegalStateException("Not found");
            }
        return new File(prefix);
    }

    private void showMultiUploadProgress(MultipleFileUpload multi_upload) throws SdkClientException, InterruptedException {
        Collection<? extends Upload> sub_xfers = multi_upload.getSubTransfers();

        do {
            for (Upload u : sub_xfers) {
                myLogger.message(u.getDescription());
                if (u.isDone()) {
                    myLogger.message(String.format("%s - %s", u.getDescription(), u.getState()));
                } else {
                    TransferProgress progress = u.getProgress();
                    double pct = progress.getPercentTransferred();
                    myLogger.message(String.format("%d%% completed", (int) pct));
                }
            }
            // `wait` a bit before the next update.
            Thread.sleep(500);
        } while (multi_upload.isDone() == false);
        Transfer.TransferState xfer_state = multi_upload.getState();
        if (!Transfer.TransferState.Completed.equals(xfer_state))
            throw new SdkClientException(String.format("Error occurred while transferring data. Status is: %s",  multi_upload.getState()));
    }
}