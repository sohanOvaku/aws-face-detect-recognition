package com.sohan.face_detect.services;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.model.*;
import com.amazonaws.services.rekognition.model.S3Object;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class FaceRecognitionService {
    @Autowired
    private AmazonRekognition rekognitionClient;

    @Autowired
    private AmazonS3 s3Client;

    public void createCollection(String collectionId) {
        CreateCollectionRequest request = new CreateCollectionRequest()
                .withCollectionId(collectionId);
        CreateCollectionResult result = rekognitionClient.createCollection(request);
        System.out.println("Collection ARN: " + result.getCollectionArn());
        System.out.println("Status code: " + result.getStatusCode());
    }

    public void deleteCollection(String collectionId) {
        DeleteCollectionRequest request = new DeleteCollectionRequest()
                .withCollectionId(collectionId);
        DeleteCollectionResult result = rekognitionClient.deleteCollection(request);
        System.out.println("Status code: " + result.getStatusCode());
    }

    public void indexFaces(String collectionId, String bucket, String photo) {
        IndexFacesRequest request = new IndexFacesRequest()
                .withCollectionId(collectionId)
                .withImage(new Image().withS3Object(new S3Object().withBucket(bucket).withName(photo)))
                .withExternalImageId(sanitizeExternalImageId(photo))
                .withDetectionAttributes("ALL");

        IndexFacesResult result = rekognitionClient.indexFaces(request);
        System.out.println("Results for " + photo);

        List<FaceRecord> faceRecords = result.getFaceRecords();
        for (FaceRecord faceRecord : faceRecords) {
            System.out.println("  Face ID: " + faceRecord.getFace().getFaceId());
            System.out.println("  Location: " + faceRecord.getFace().getBoundingBox());

            // Check if the face is unique
            boolean isUnique = isUniqueFace(collectionId, faceRecord.getFace().getFaceId());

            if (isUnique) {
                try {
                    // Crop and upload the face image
                    cropFaceAndUpload(bucket, photo, faceRecord.getFace().getBoundingBox(), faceRecord.getFace().getFaceId());
                } catch (Exception e) {
                    System.out.println("Failed to store face: " + e.getMessage());
                }
            }
        }
    }

    public void indexFacesInFolder(String collectionId, String bucket, String folder) {
        ObjectListing objectListing = s3Client.listObjects(bucket, folder);
        List<S3ObjectSummary> objectSummaries = objectListing.getObjectSummaries();
        for (S3ObjectSummary objectSummary : objectSummaries) {
            if (!objectSummary.getKey().endsWith("/")) { // Skip folders
                indexFaces(collectionId, bucket, objectSummary.getKey());
            }
        }
    }

    private void cropFaceAndUpload(String bucket, String key, BoundingBox boundingBox, String faceId) throws Exception {
        com.amazonaws.services.s3.model.S3Object s3Object = s3Client.getObject(bucket, key);
        InputStream objectData = s3Object.getObjectContent();
        BufferedImage originalImage = ImageIO.read(objectData);

        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        int left = (int) (boundingBox.getLeft() * width);
        int top = (int) (boundingBox.getTop() * height);
        int faceWidth = (int) (boundingBox.getWidth() * width);
        int faceHeight = (int) (boundingBox.getHeight() * height);

        BufferedImage croppedImage = originalImage.getSubimage(left, top, faceWidth, faceHeight);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(croppedImage, "jpg", os);
        InputStream inputStream = new ByteArrayInputStream(os.toByteArray());

        String croppedKey = "faces/" + faceId + ".jpg";
        s3Client.putObject(bucket, croppedKey, inputStream, null);
    }

    private boolean isUniqueFace(String collectionId, String faceId) {
        SearchFacesRequest searchFacesRequest = new SearchFacesRequest()
                .withCollectionId(collectionId)
                .withFaceId(faceId)
                .withFaceMatchThreshold(90F) // Confidence threshold
                .withMaxFaces(1);

        SearchFacesResult searchFacesResult = rekognitionClient.searchFaces(searchFacesRequest);
        List<FaceMatch> faceMatches = searchFacesResult.getFaceMatches();

        return faceMatches.isEmpty();
    }
    public List<String> searchFacesByImage(String collectionId, String bucket,String folder, String photo) {
        SearchFacesByImageRequest request = new SearchFacesByImageRequest()
                .withCollectionId(collectionId)
                .withImage(new Image().withS3Object(new S3Object().withBucket(bucket).withName(folder+"/"+photo)))
                .withMaxFaces(5)
                .withFaceMatchThreshold(90F);
        SearchFacesByImageResult result = rekognitionClient.searchFacesByImage(request);
        List<FaceMatch> faceMatches = result.getFaceMatches();
        System.out.println("Matching faces:");
        List<String> faceUrls = new ArrayList<>();
        for (FaceMatch match : faceMatches) {
            System.out.println("  FaceId: " + match.getFace().getFaceId());
            System.out.println("  Similarity: " + match.getSimilarity() + "%");
            System.out.println("  ImageId: " + match.getFace().getExternalImageId());
            // Generate public access URL for the matched image
            String matchedImageKey = unsanitizeExternalImageId(match.getFace().getExternalImageId());
            URL publicUrl = generatePresignedUrl(bucket, matchedImageKey);
            faceUrls.add(publicUrl.toString());
        }
        return faceUrls;
    }

    private String sanitizeExternalImageId(String imageId) {
        return imageId.replaceAll("[^a-zA-Z0-9_.\\-:]", "_");
    }

    private String unsanitizeExternalImageId(String imageId) {
        return imageId.replaceFirst("_", "/");
    }

    public List<String> getAllFaceImageNames(String bucket) {
        ListObjectsV2Request req = new ListObjectsV2Request().withBucketName(bucket).withPrefix("faces/");
        ListObjectsV2Result result = s3Client.listObjectsV2(req);
        List<String> faceImageNames = new ArrayList<>();
        for (S3ObjectSummary objectSummary : result.getObjectSummaries()) {
            String faceId = objectSummary.getKey().replace("faces/", "").replace(".jpg", "");
            faceImageNames.add(faceId);
        }
        return faceImageNames;
    }

    public List<String> getAllFaceImageUrls(String bucket) {
        ListObjectsV2Request req = new ListObjectsV2Request().withBucketName(bucket).withPrefix("faces/");
        ListObjectsV2Result result = s3Client.listObjectsV2(req);
        List<String> faceImageUrls = new ArrayList<>();
        for (S3ObjectSummary objectSummary : result.getObjectSummaries()) {
            URL publicUrl = generatePresignedUrl(bucket, objectSummary.getKey());
            faceImageUrls.add(publicUrl.toString());
        }
        return faceImageUrls;
    }

    public List<String> getAllImageUrls(String bucket, String folder) {
        ListObjectsV2Request req = new ListObjectsV2Request().withBucketName(bucket).withPrefix(folder + "/");
        ListObjectsV2Result result = s3Client.listObjectsV2(req);
        List<String> imageUrls = new ArrayList<>();
        for (S3ObjectSummary objectSummary : result.getObjectSummaries()) {
            URL publicUrl = generatePresignedUrl(bucket, objectSummary.getKey());
            imageUrls.add(publicUrl.toString());
        }
        return imageUrls;
    }

    private URL generatePresignedUrl(String bucket, String key) {
        Date expiration = new Date();
        long expTimeMillis = expiration.getTime();
        expTimeMillis += 1000 * 60 * 60; // 1 hour
        expiration.setTime(expTimeMillis);

        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(bucket, key)
                        .withMethod(HttpMethod.GET)
                        .withExpiration(expiration);

        return s3Client.generatePresignedUrl(generatePresignedUrlRequest);
    }
}
