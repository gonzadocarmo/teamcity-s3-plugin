package com.dcn.teamcity.awsS3Plugin;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.dcn.teamcity.awsS3Plugin.adapters.AWSS3Adapter;
import jetbrains.buildServer.ExtensionHolder;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.BuildProcessAdapter;
import jetbrains.buildServer.agent.BuildProgressLogger;
import jetbrains.buildServer.agent.impl.artifacts.ArtifactsCollection;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 *
 * @author <a href="mailto:gonzalo.docarmo@gmail.com">Gonzalo G. do Carmo Norte</a>
 */
public class AWSS3BuildProcessAdapter extends BuildProcessAdapter {

    protected final BuildProgressLogger myLogger;
    private volatile boolean hasFinished;
    private volatile boolean hasFailed;
    private volatile boolean isInterrupted;
    private final Map<String, String> runnerParametersMap;
    private final File checkoutDirectory;
    private final ExtensionHolder extensionHolder;
    private final AWSS3Adapter awsS3Adapter;
    private final AWSS3BuildProcessAdapterHelper helper;
    private final String TASK_COMPLETED_TEXT = "Done";
    private final String TASK_FAILED_TEXT = "Failed: ";
    private final String ERROR_PROXY = "HTTP Proxy URL not valid: '%s'. Skipping...";

    public AWSS3BuildProcessAdapter(@NotNull final BuildProgressLogger logger,
                                    @NotNull final Map<String, String> runnerParameters,
                                    @NotNull final File agentCheckoutDirectory,
                                    @NotNull final ExtensionHolder extensionHolder,
                                    @NotNull final AWSS3Adapter awsS3Adapter,
                                    @NotNull final AWSS3BuildProcessAdapterHelper processAdapterHelper) {
        myLogger = logger;
        hasFinished = false;
        hasFailed = false;
        runnerParametersMap = runnerParameters;
        checkoutDirectory = agentCheckoutDirectory;
        this.extensionHolder = extensionHolder;
        this.awsS3Adapter = awsS3Adapter;
        helper = processAdapterHelper;
    }


    @Override
    public void interrupt() {
        isInterrupted = true;
    }

    @Override
    public boolean isInterrupted() {
        return isInterrupted;
    }

    @Override
    public boolean isFinished() {
        return hasFinished;
    }

    @Override
    public @NotNull BuildFinishedStatus waitFor() throws RunBuildException {
        while (!isInterrupted() && !hasFinished) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RunBuildException(e);
            }
        }
        return hasFinished ?
                hasFailed ? BuildFinishedStatus.FINISHED_FAILED :
                        BuildFinishedStatus.FINISHED_SUCCESS :
                BuildFinishedStatus.INTERRUPTED;
    }

    @Override
    public void start() throws RunBuildException {
        try {
            hasFailed = !runProcess();
            hasFinished = true;
        } catch (RuntimeException e) {
            hasFinished = false;
            myLogger.error("Failed: " + e.getMessage());
        }
    }

    /**
     * @return true is process finished successfully
     */
    public boolean runProcess() {
        final AgentRunnerBuildParametersModel model = helper.createModel(runnerParametersMap);

        AmazonS3 s3Client = createClient(model.getPublicKey(), model.getPrivateKey(), model.getBucketRegion(), model.getHttpProxy());

        emptyBucketIfNeeded(model.getBucketName(), s3Client, model.isEmptyBucketBeforeUpload());
        uploadFilesToBucket(model.getBucketName(), model.getArtifactsPath(), s3Client, model.getHttpHeaderCacheControl());

        return true;
    }

    @NotNull
    private AmazonS3 createClient(String publicKey, String privateKey, String region, String httpProxy) {
        AmazonS3 s3Client;
        if (httpProxy != null) {
            try {
                s3Client = helper.createClientWithProxy(publicKey, privateKey, region, httpProxy);
            } catch (Exception e) {
                myLogger.warning(String.format(ERROR_PROXY, httpProxy));
                s3Client = helper.createClient(publicKey, privateKey, region);
            }
        } else {
            s3Client = helper.createClient(publicKey, privateKey, region);
        }
        return s3Client;
    }

    private void uploadFilesToBucket(String bucketName, String sourcePaths, AmazonS3 s3Client, String cacheControlHeader) {
        final String TARGET_NAME = "Upload to S3 bucket: " + bucketName;
        myLogger.targetStarted(TARGET_NAME);

        int totalCount = 0;
        final List<ArtifactsCollection> myArtifacts = helper.getArtifactsCollections(sourcePaths, extensionHolder, checkoutDirectory);
        for (ArtifactsCollection artifactsCollection : myArtifacts) {
            int count = 0;
            for (Map.Entry<File, String> fileStringEntry : artifactsCollection.getFilePathMap().entrySet()) {
                final File source = fileStringEntry.getKey();

                uploadFile(bucketName, s3Client, source, fileStringEntry.getValue(), cacheControlHeader);
                checkIsInterrupted();

                count++;
                totalCount++;
            }
            myLogger.message("Uploaded [" + count + "] files for [" + artifactsCollection.getSourcePath() + "] pattern");
        }

        if (totalCount > 0) {
            myLogger.message(TASK_COMPLETED_TEXT);
        } else {
            interrupt();
            final String errorMsg = "No files to upload have been found!";
            myLogger.error(TASK_FAILED_TEXT + errorMsg);
            myLogger.logBuildProblem(helper.createBuildProblemData(bucketName, errorMsg));
        }
        myLogger.targetFinished(TARGET_NAME);
    }

    private void emptyBucketIfNeeded(String bucketName, AmazonS3 s3Client, boolean emptyBucket) {
        final String TARGET_NAME = "Empty S3 bucket: " + bucketName;

        myLogger.targetStarted(TARGET_NAME);
        if (emptyBucket) {
            emptyBucket(bucketName, s3Client);
            checkIsInterrupted();
        } else {
            myLogger.message("Option not set. Skipping...");
        }
        myLogger.targetFinished(TARGET_NAME);
    }

    private void uploadFile(String bucketName, AmazonS3 client, File source, String fileStringEntryValue, String cacheControlHeader) {
        myLogger.message("Transferring [" + source.getAbsolutePath() + "] to [" + fileStringEntryValue + "]");

        try {
            awsS3Adapter.getUploadAdapter().uploadToBucket(bucketName, client, source, fileStringEntryValue, cacheControlHeader);
            myLogger.message("done transferring [" + source.getPath() + "]");
        } catch (AmazonClientException ace) {
            interrupt();
            myLogger.error(TASK_FAILED_TEXT + ace.getMessage());
            myLogger.logBuildProblem(helper.createBuildProblemData(bucketName, source.getPath(), ace.getMessage()));
        }
    }

    protected void checkIsInterrupted() throws RuntimeException {
        if (isInterrupted()) {
            throw new RuntimeException();
        }
    }

    private void emptyBucket(String bucketName, AmazonS3 client) {
        try {
            awsS3Adapter.getDeleteAdapter().deleteAllContentFromBucket(bucketName, client);
            myLogger.message(TASK_COMPLETED_TEXT);
        } catch (AmazonClientException ace) {
            interrupt();
            myLogger.error(TASK_FAILED_TEXT + ace.getMessage());
            myLogger.logBuildProblem(helper.createBuildProblemData(bucketName, ace.getMessage()));
        }
    }

    @Override
    public int hashCode() {
        int a = this.isFinished() ? 1 : 3;
        int b = this.isInterrupted() ? 5 : 7;
        int c = this.hasFailed ? 11 : 13;
        return Long.valueOf((a * 31 + b) * 31 + c).hashCode();
    }

    @Override
    public String toString() {
        return String.format("[AWSS3BuildProcessAdapter] Finished: %s, failed: %s, interrupted: %s - checkout dir: %s",
                this.isFinished(),
                this.hasFailed,
                this.isInterrupted(),
                this.checkoutDirectory.getPath());
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null &&
                obj instanceof AWSS3BuildProcessAdapter &&
                this.isFinished() == ((AWSS3BuildProcessAdapter) obj).isFinished() &&
                this.hasFailed == ((AWSS3BuildProcessAdapter) obj).hasFailed &&
                this.isInterrupted() == ((AWSS3BuildProcessAdapter) obj).isInterrupted() &&
                this.checkoutDirectory.equals(((AWSS3BuildProcessAdapter) obj).checkoutDirectory);
    }
}