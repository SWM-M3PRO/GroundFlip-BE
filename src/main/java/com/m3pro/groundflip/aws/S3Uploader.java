package com.m3pro.groundflip.aws;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;

import lombok.RequiredArgsConstructor;

@Service
@Component
@RequiredArgsConstructor
public class S3Uploader {
	private final AmazonS3Client amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public String uploadFiles(MultipartFile multipartFile) throws IOException{
        String fileName = multipartFile.getOriginalFilename();
        assert fileName != null : "fileName must not null";
		String fileExtention = fileName.substring(fileName.lastIndexOf("."));
        String originalFilename = convertFiletoUUID(fileName).concat(fileExtention);

        String path = bucket.concat("/static");

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(multipartFile.getSize());
        metadata.setContentType(multipartFile.getContentType());

        amazonS3Client.putObject(path, originalFilename,multipartFile.getInputStream(), metadata);
        return amazonS3Client.getUrl(path, originalFilename).toString();
    }

    private String convertFiletoUUID(String fileName){
        return UUID.randomUUID().toString();
    }

}
