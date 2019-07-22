package com.dcn.teamcity.awsS3Plugin.adapters;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import jetbrains.buildServer.util.StringUtil;

import java.io.File;

/**
 * @author <a href="mailto:gonzalo.docarmo@gmail.com">Gonzalo G. do Carmo Norte</a>
 */
public class AWSS3UploadAdapter {

    public void uploadToBucket(String bucketName, AmazonS3 s3Client, File file, String destinationDir, String cacheControlString) throws AmazonClientException {
        String key = StringUtil.nullIfEmpty(destinationDir) == null ? file.getName() : destinationDir + "/" + file.getName();

        PutObjectRequest request = new PutObjectRequest(
                bucketName, key, file);

        if (cacheControlString != null) {
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setCacheControl(cacheControlString);
            request.withMetadata(objectMetadata);
        }
        s3Client.putObject(request);
    }
}
