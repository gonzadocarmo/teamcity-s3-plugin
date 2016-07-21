package com.dcn.teamcity.awsS3Plugin.adapters;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Created by sg0216948 on 7/13/16.
 *
 * @author <a href="mailto:gonzalo.docarmo@gmail.com">Gonzalo G. do Carmo Norte</a>
 */
public class AWSS3UploadAdapterTest {

    private AWSS3UploadAdapter adapter;
    private AmazonS3 amazonS3Client;
    private String bucketName;
    private File source;
    private String httpHeaderCacheControl;

    @BeforeMethod
    public void setUp() throws Exception {
        bucketName = "my-bucket";
        adapter = new AWSS3UploadAdapter();
        amazonS3Client = mock(AmazonS3.class);
        source = new File("/tmp");
        httpHeaderCacheControl = null;
    }

    @Test
    public void testUploadToBucketWhenNoDestinationDir() throws Exception {
        final String destinationDir = null;

        adapter.uploadToBucket(bucketName, amazonS3Client, source, destinationDir, httpHeaderCacheControl);

        ArgumentCaptor<PutObjectRequest> argument = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(amazonS3Client).putObject(argument.capture());

        assertEquals(bucketName, argument.getValue().getBucketName());
        assertEquals(source.getName(), argument.getValue().getKey());
        assertEquals(source, argument.getValue().getFile());
        assertNull(argument.getValue().getMetadata());
    }

    @Test
    public void testUploadToBucketWhenEmptyDestinationDir() throws Exception {
        final String destinationDir = "";

        adapter.uploadToBucket(bucketName, amazonS3Client, source, destinationDir, httpHeaderCacheControl);

        ArgumentCaptor<PutObjectRequest> argument = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(amazonS3Client).putObject(argument.capture());

        assertEquals(bucketName, argument.getValue().getBucketName());
        assertEquals(source.getName(), argument.getValue().getKey());
        assertEquals(source, argument.getValue().getFile());
        assertNull(argument.getValue().getMetadata());
    }

    @Test
    public void testUploadToBucketWhenSomeDestinationDir() throws Exception {
        final String destinationDir = "src/here";

        adapter.uploadToBucket(bucketName, amazonS3Client, source, destinationDir, httpHeaderCacheControl);

        ArgumentCaptor<PutObjectRequest> argument = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(amazonS3Client).putObject(argument.capture());

        assertEquals(bucketName, argument.getValue().getBucketName());
        assertEquals(destinationDir + "/" + source.getName(), argument.getValue().getKey());
        assertEquals(source, argument.getValue().getFile());
        assertNull(argument.getValue().getMetadata());
    }

    @Test
    public void testUploadToBucketWhenHttpHeaderCacheControlPresent() throws Exception {
        final String destinationDir = "src/here";
        httpHeaderCacheControl = "max-age=0, no-cache, no-store";

        adapter.uploadToBucket(bucketName, amazonS3Client, source, destinationDir, httpHeaderCacheControl);

        ArgumentCaptor<PutObjectRequest> argument = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(amazonS3Client).putObject(argument.capture());

        assertEquals(bucketName, argument.getValue().getBucketName());
        assertEquals(destinationDir + "/" + source.getName(), argument.getValue().getKey());
        assertEquals(source, argument.getValue().getFile());
        assertEquals(httpHeaderCacheControl, argument.getValue().getMetadata().getCacheControl());
    }
}
