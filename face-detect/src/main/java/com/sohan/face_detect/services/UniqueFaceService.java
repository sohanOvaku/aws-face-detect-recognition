//package com.sohan.face_detect.services;
//
//import com.amazonaws.services.rekognition.model.BoundingBox;
//import com.amazonaws.services.rekognition.model.FaceDetail;
//import com.amazonaws.services.s3.AmazonS3;
//import com.amazonaws.services.s3.model.CopyObjectRequest;
//import com.amazonaws.services.s3.model.S3ObjectSummary;
//import org.springframework.beans.factory.annotation.Autowired;
//
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//
//public class UniqueFaceService {
//    private final S3Service s3Service;
//    private final RekognitionService rekognitionService;
//    private final AmazonS3 s3Client;
//
//    @Autowired
//    public UniqueFaceService(S3Service s3Service, RekognitionService rekognitionService, AmazonS3 s3Client) {
//        this.s3Service = s3Service;
//        this.rekognitionService = rekognitionService;
//        this.s3Client = s3Client;
//    }
//
//    public List<String> processImages(String sourceBucket, String destinationBucket) {
//        List<S3ObjectSummary> objects = s3Service.listObjects(sourceBucket);
//        Set<String> uniqueFaces = new HashSet<>();
//
//        for (S3ObjectSummary object : objects) {
//            List<FaceDetail> faceDetails = rekognitionService.detectFaces(sourceBucket, object.getKey());
//
//            for (FaceDetail faceDetail : faceDetails) {
//                BoundingBox boundingBox = faceDetail.getBoundingBox();
//
////                if (!uniqueFaces.contains(faceId)) {
////                    uniqueFaces.add(faceId);
////                    // Code to copy image to destination bucket
////                    copyImageToDestination(sourceBucket, object.getKey(), destinationBucket, object.getKey());
////                }
//                if (isUniqueFace(uniqueFaces, boundingBox)) {
//                    uniqueFaces.add(boundingBox);
//                    // Copy image to destination bucket
//                    copyImageToDestination(sourceBucket, object.getKey(), destinationBucket, object.getKey());
//                }
//            }
//        }
//
//        return uniqueFaces.stream().map(faceId -> getImageUrl(destinationBucket, faceId)).collect(Collectors.toList());
//    }
//    private boolean isUniqueFace(Set<BoundingBox> uniqueFaces, BoundingBox boundingBox) {
//        // Implement your own logic to compare bounding boxes and determine uniqueness
//        // For simplicity, you can use bounding box coordinates directly
//        return uniqueFaces.stream().noneMatch(b -> b.equals(boundingBox));
//    }
//
//    private void copyImageToDestination(String sourceBucket, String sourceKey, String destinationBucket, String destinationKey) {
//        CopyObjectRequest copyObjectRequest = new CopyObjectRequest(sourceBucket, sourceKey, destinationBucket, destinationKey);
//        s3Client.copyObject(copyObjectRequest);
//    }
//
//    private String getImageUrl(String bucket, String key) {
//        return s3Client.getUrl(bucket, key).toString();
//    }
//}
