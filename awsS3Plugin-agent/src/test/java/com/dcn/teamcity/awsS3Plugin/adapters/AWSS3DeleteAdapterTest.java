package com.dcn.teamcity.awsS3Plugin.adapters;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import org.jetbrains.annotations.NotNull;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Created by sg0216948 on 7/13/16.
 *
 * @author <a href="mailto:gonzalo.docarmo@gmail.com">Gonzalo G. do Carmo Norte</a>
 */
public class AWSS3DeleteAdapterTest {

    private AWSS3DeleteAdapter adapter;
    private AmazonS3 amazonS3Client;
    private String bucketName;

    @BeforeMethod
    public void setUp() throws Exception {
        bucketName = "my-bucket";
        adapter = new AWSS3DeleteAdapter();
        amazonS3Client = mock(AmazonS3.class);
    }

    @Test
    public void tesDeleteAllContentFromBucket() throws Exception {
        ObjectListing objectListing = createObjectListing();

        when(amazonS3Client.listObjects(any(String.class))).thenReturn(objectListing);
        when(amazonS3Client.listVersions(any(ListVersionsRequest.class))).thenReturn(new VersionListing());


        adapter.deleteAllContentFromBucket(bucketName, amazonS3Client);

        verify(amazonS3Client, times(objectListing.getObjectSummaries().size())).deleteObject(any(String.class), any(String.class));
        verify(amazonS3Client).deleteObject(bucketName, objectListing.getObjectSummaries().get(0).getKey());
        verify(amazonS3Client).deleteObject(bucketName, objectListing.getObjectSummaries().get(1).getKey());
        verify(amazonS3Client).deleteObject(bucketName, objectListing.getObjectSummaries().get(2).getKey());

        ArgumentCaptor<ListVersionsRequest> argument = ArgumentCaptor.forClass(ListVersionsRequest.class);
        verify(amazonS3Client).listVersions(argument.capture());
        assertEquals(bucketName, argument.getValue().getBucketName());

        verify(amazonS3Client, never()).deleteVersion(anyString(), anyString(), anyString());
    }

    @Test
    public void tesDeleteAllContentFromBucketWithVersioning() throws Exception {
        ObjectListing objectListing = createObjectListing();
        VersionListing versionListing = createVersionListing();

        when(amazonS3Client.listObjects(any(String.class))).thenReturn(objectListing);
        when(amazonS3Client.listVersions(any(ListVersionsRequest.class))).thenReturn(versionListing);


        adapter.deleteAllContentFromBucket(bucketName, amazonS3Client);

        ArgumentCaptor<ListVersionsRequest> argument = ArgumentCaptor.forClass(ListVersionsRequest.class);
        verify(amazonS3Client).listVersions(argument.capture());
        assertEquals(bucketName, argument.getValue().getBucketName());

        verify(amazonS3Client, times(versionListing.getVersionSummaries().size())).deleteVersion(any(String.class), any(String.class), any(String.class));
        verify(amazonS3Client).deleteVersion(bucketName, versionListing.getVersionSummaries().get(0).getKey(), versionListing.getVersionSummaries().get(0).getVersionId());
        verify(amazonS3Client).deleteVersion(bucketName, versionListing.getVersionSummaries().get(1).getKey(), versionListing.getVersionSummaries().get(1).getVersionId());
    }

    @NotNull
    private VersionListing createVersionListing() {
        S3VersionSummary s3VersionSummary1 = new S3VersionSummary();
        s3VersionSummary1.setKey("key1");
        s3VersionSummary1.setVersionId("version11");
        S3VersionSummary s3VersionSummary2 = new S3VersionSummary();
        s3VersionSummary2.setKey("key2");
        s3VersionSummary2.setVersionId("version22");

        VersionListing versionListing = new VersionListing();
        versionListing.getVersionSummaries().add(s3VersionSummary1);
        versionListing.getVersionSummaries().add(s3VersionSummary2);
        return versionListing;
    }

    @NotNull
    private ObjectListing createObjectListing() {
        S3ObjectSummary s3ObjectSummary1 = new S3ObjectSummary();
        s3ObjectSummary1.setKey("one");
        S3ObjectSummary s3ObjectSummary2 = new S3ObjectSummary();
        s3ObjectSummary2.setKey("two");
        S3ObjectSummary s3ObjectSummary3 = new S3ObjectSummary();
        s3ObjectSummary3.setKey("three");

        ObjectListing objectListing = new ObjectListing();
        objectListing.getObjectSummaries().add(s3ObjectSummary1);
        objectListing.getObjectSummaries().add(s3ObjectSummary2);
        objectListing.getObjectSummaries().add(s3ObjectSummary3);
        return objectListing;
    }
}