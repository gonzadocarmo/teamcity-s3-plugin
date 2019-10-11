package com.dcn.teamcity.awsS3Plugin.adapters;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.util.StringUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author <a href="mailto:gonzalo.docarmo@gmail.com">Gonzalo G. do Carmo Norte</a>
 */
public class AWSS3UploadAdapter {

    public void uploadToBucket(String bucketName, AmazonS3 s3Client, File file, String destinationDir, String cacheControlString) throws AmazonClientException {
        String key = StringUtil.nullIfEmpty(destinationDir) == null ? file.getName() : destinationDir + "/" + file.getName();

        PutObjectRequest request = new PutObjectRequest(
                bucketName, key, file);

        final ObjectMetadata objectMetadata = buildMetadata(file, cacheControlString);
        request.withMetadata(objectMetadata);

        s3Client.putObject(request);
    }

    private ObjectMetadata buildMetadata(File file, String cacheControlString) {
        final ObjectMetadata objectMetadata = new ObjectMetadata();

        final Path filePath = file.toPath();
        try {
            final String contentType = Files.probeContentType(filePath);
            objectMetadata.setContentType(contentType);
        } catch (IOException e) {
            Loggers.SERVER.warn("Unable to get content type for file " + filePath, e);
        }

        if (cacheControlString != null) {
            objectMetadata.setCacheControl(cacheControlString);
        }

        return objectMetadata;
    }
}
