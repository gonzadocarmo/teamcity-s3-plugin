package com.dcn.teamcity.awsS3Plugin.adapters;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.util.StringUtil;
import org.apache.tika.Tika;

import java.io.File;
import java.io.IOException;

/**
 * @author <a href="mailto:gonzalo.docarmo@gmail.com">Gonzalo G. do Carmo Norte</a>
 */
public class AWSS3UploadAdapter {

    private final Tika tika = new Tika();

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

        try {
            final String contentType = tika.detect(file);
            objectMetadata.setContentType(contentType);
        } catch (IOException e) {
            Loggers.SERVER.warn("Unable to get content type for file " + file.getPath(), e);
        }

        if (cacheControlString != null) {
            objectMetadata.setCacheControl(cacheControlString);
        }

        return objectMetadata;
    }
}
