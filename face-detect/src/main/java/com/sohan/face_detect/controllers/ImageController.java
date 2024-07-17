package com.sohan.face_detect.controllers;

import com.sohan.face_detect.services.FaceRecognitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/images")
public class ImageController {
    @Autowired
    private FaceRecognitionService faceRecognitionService;

    @GetMapping("/faces/names")
    public List<String> getAllFaceImageNames(@RequestParam String bucket) {
        return faceRecognitionService.getAllFaceImageNames(bucket);
    }

    @GetMapping("/faces")
    public List<String> getAllFaceImageUrls(@RequestParam String bucket) {
        return faceRecognitionService.getAllFaceImageUrls(bucket);
    }

    @GetMapping("")
    public List<String> getAllImageUrls(@RequestParam String bucket, @RequestParam String folder) {
        return faceRecognitionService.getAllImageUrls(bucket, folder);
    }
}
