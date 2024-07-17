package com.sohan.face_detect.services;

import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.model.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class RekognitionService {
    private final AmazonRekognition rekognitionClient;

    @Autowired
    public RekognitionService(AmazonRekognition rekognitionClient) {
        this.rekognitionClient = rekognitionClient;
    }

    public List<FaceDetail> detectFaces(String bucketName, String key) {
        DetectFacesRequest request = new DetectFacesRequest()
                .withImage(new Image()
                        .withS3Object(new S3Object()
                                .withBucket(bucketName)
                                .withName(key)))
                .withAttributes("ALL");

        DetectFacesResult result = rekognitionClient.detectFaces(request);
        return result.getFaceDetails();
    }
}
