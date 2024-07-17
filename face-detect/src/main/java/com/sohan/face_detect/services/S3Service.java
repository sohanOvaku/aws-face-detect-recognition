package com.sohan.face_detect.services;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class S3Service {
    private final AmazonS3 s3Client;

    @Autowired
    public S3Service(AmazonS3 s3Client) {
        this.s3Client = s3Client;
    }

    public List<S3ObjectSummary> listObjects(String bucketName) {
        ListObjectsV2Request req = new ListObjectsV2Request().withBucketName(bucketName);
        ListObjectsV2Result result;
        result = s3Client.listObjectsV2(req);
        return result.getObjectSummaries();
    }
}
