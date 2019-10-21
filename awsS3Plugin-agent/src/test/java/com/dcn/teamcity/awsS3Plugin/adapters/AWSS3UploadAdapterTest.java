package com.dcn.teamcity.awsS3Plugin.adapters;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermissions;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
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
        source = File.createTempFile("temp", ".txt");
        source.deleteOnExit();
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
        final PutObjectRequest request = argument.getValue();
        assertNotNull(request);
        final ObjectMetadata metadata = request.getMetadata();
        assertNotNull(metadata);
        assertNotNull(metadata.getContentType());
        assertNull(metadata.getCacheControl());
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
        final PutObjectRequest request = argument.getValue();
        assertNotNull(request);
        final ObjectMetadata metadata = request.getMetadata();
        assertNotNull(metadata);
        assertNotNull(metadata.getContentType());
        assertNull(metadata.getCacheControl());
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
        final PutObjectRequest request = argument.getValue();
        assertNotNull(request);
        final ObjectMetadata metadata = request.getMetadata();
        assertNotNull(metadata);
        assertNotNull(metadata.getContentType());
        assertNull(metadata.getCacheControl());
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

    @Test
    public void testUploadToBucketWhenContentTypeDetected() throws Exception {
        final String contentType = "text/plain";
        final String destinationDir = "src/here";

        adapter.uploadToBucket(bucketName, amazonS3Client, source, destinationDir, httpHeaderCacheControl);

        ArgumentCaptor<PutObjectRequest> argument = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(amazonS3Client).putObject(argument.capture());

        final PutObjectRequest request = argument.getValue();
        assertEquals(bucketName, request.getBucketName());
        assertEquals(destinationDir + "/" + source.getName(), request.getKey());
        assertEquals(source, request.getFile());
        assertEquals(contentType, request.getMetadata().getContentType());
    }

    @Test
    public void testUploadToBucketWhenContentTypeThrowsIOException() throws Exception {
        final File source = File.createTempFile("temp", ".bla");
        Files.setPosixFilePermissions(source.toPath(), PosixFilePermissions.fromString("---------"));
        final String destinationDir = "src/here";

        adapter.uploadToBucket(bucketName, amazonS3Client, source, destinationDir, httpHeaderCacheControl);

        ArgumentCaptor<PutObjectRequest> argument = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(amazonS3Client).putObject(argument.capture());

        final PutObjectRequest request = argument.getValue();
        assertEquals(bucketName, request.getBucketName());
        assertEquals(destinationDir + "/" + source.getName(), request.getKey());
        assertEquals(source, request.getFile());
        assertNull(request.getMetadata().getContentType());
    }
}
