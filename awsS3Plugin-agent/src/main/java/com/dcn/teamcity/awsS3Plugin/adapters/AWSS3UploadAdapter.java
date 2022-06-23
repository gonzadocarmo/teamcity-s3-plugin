package com.dcn.teamcity.awsS3Plugin.adapters;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.transfer.*;
import jetbrains.buildServer.agent.BuildProgressLogger;
import jetbrains.buildServer.agent.impl.artifacts.ArtifactsCollection;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

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
        List<File> filesList = Arrays.asList(artifactsCollection.getFilePathMap().keySet().toArray(new File[0]));
        File firstFile = filesList.get(0);
        String firstFilePath = artifactsCollection.getFilePathMap().values().toArray(new String[0])[0];
        
        // For example - we create artifact filter as "dist => some/dir"
        // So artifactsCollection.getTargetPath() will be "some/dir", and we could use it as virtual key prefix
        // Next, we need to get common directory path. 
        // In FilePathMap we will get something like this:
        //   /opt/teamcity/buildAgentFull/work/hash/dist/css/some.css => some/dir/css
        //   /opt/teamcity/buildAgentFull/work/hash/dist/css/some1.css => some/dir/css
        //   /opt/teamcity/buildAgentFull/work/hash/dist/js/some.js => some/dir/js
        // We need to find common directory (/opt/teamcity/buildAgentFull/work/hash/dist)
        // Length of path of file relative to key path: firstFilePath.length() - target_len;
        // For our example it will be length of string "/css"
        int target_len = artifactsCollection.getTargetPath().length();
        if (artifactsCollection.getTargetPath().endsWith(File.separator))
            target_len -= File.separator.length();
        int relativeDirLength = firstFilePath.length() - target_len;

        // Index of last char in common dir path: path.length() - (fileName.length() + 1 + relativeDirLength)

        // /opt/teamcity/buildAgentFull/work/hash/dist /css                  /some.css
        // <                commonDir                > < relativeDirLength > < firstFile.getName().length() + File.separator.length() >
        // <                commonDir                > <                                   filePath                                   >
        int filePathLen = firstFile.getName().length() + File.separator.length() + relativeDirLength;
        String common_dir_path = firstFile.getAbsolutePath().substring(0, firstFile.getAbsolutePath().length() - filePathLen);
        myLogger.message(String.format("Detected common directory: %s", common_dir_path));

        MultipleFileUpload xfer = tm.uploadFileList(bucketName, artifactsCollection.getTargetPath(), new File(common_dir_path), filesList, omp);
        showMultiUploadProgress(xfer);
        myLogger.message("Done");
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