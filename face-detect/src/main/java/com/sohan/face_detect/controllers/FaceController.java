package com.sohan.face_detect.controllers;

import com.sohan.face_detect.services.FaceRecognitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/collections")
public class FaceController {
//    @Autowired
//    private UniqueFaceService uniqueFaceService;

    @Autowired
    private FaceRecognitionService faceRecognitionService;


    @PostMapping()
    public void createCollection(@RequestParam String collectionId) {
        faceRecognitionService.createCollection(collectionId);
    }

    @DeleteMapping()
    public void deleteCollection(@RequestParam String collectionId) {
        faceRecognitionService.deleteCollection(collectionId);
    }

//    @PostMapping("/index-faces")
//    public void indexFaces(@RequestParam String collectionId, @RequestParam String bucket, @RequestParam String photo) {
//        faceRecognitionService.indexFaces(collectionId, bucket, photo);
//    }

    @PostMapping("/faces")
    public void indexFacesInFolder(@RequestParam String collectionId, @RequestParam String bucket, @RequestParam String folder) {
        faceRecognitionService.indexFacesInFolder(collectionId, bucket, folder);
    }

    @GetMapping("/faces")
    public List<String> searchFaces(@RequestParam String collectionId, @RequestParam String bucket, @RequestParam String folder, @RequestParam String photo) {
        return faceRecognitionService.searchFacesByImage(collectionId, bucket, folder, photo);
    }
}
