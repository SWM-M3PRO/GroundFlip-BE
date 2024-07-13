package com.m3pro.groundflip.aws;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController

public class S3Controller {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final S3Uploader s3Uploader;

    @PostMapping("/{userId}/image")
    public void updateUserImage(@RequestParam("images") MultipartFile multipartFile) {
        try {
            s3Uploader.uploadFiles(multipartFile, "static");
        } catch (Exception e) {
            log.info("s3 response {}", new ResponseEntity(HttpStatus.BAD_REQUEST));
        }
    }
}