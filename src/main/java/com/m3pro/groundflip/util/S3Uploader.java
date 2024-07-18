package com.m3pro.groundflip.util;

import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.m3pro.groundflip.exception.AppException;
import com.m3pro.groundflip.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@Component
@RequiredArgsConstructor
public class S3Uploader {
	private final AmazonS3Client amazonS3Client;

	@Value("${cloud.aws.s3.bucket}")
	private String bucket;

	public String uploadFiles(MultipartFile multipartFile) throws IOException {
		String originalFileName = multipartFile.getOriginalFilename();
		String convertedFileName;

		if (originalFileName != null) {
			convertedFileName = convertFileNameToUuid(originalFileName);
		} else {
			throw new AppException(ErrorCode.IMAGE_NOT_FOUND);
		}

		String path = bucket.concat("/static");

		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(multipartFile.getSize());
		metadata.setContentType(multipartFile.getContentType());

		amazonS3Client.putObject(path, convertedFileName, multipartFile.getInputStream(), metadata);
		return amazonS3Client.getUrl(path, convertedFileName).toString();
	}

	private String convertFileNameToUuid(String fileName) {
		String fileExtension = fileName.substring(fileName.lastIndexOf("."));
		return UUID.randomUUID().toString().concat(fileExtension);
	}

}
