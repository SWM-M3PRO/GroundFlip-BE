package com.m3pro.groundflip.util;

import java.io.IOException;
import java.util.UUID;

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

	// public String uploadFiles(MultipartFile multipartFile, Long userId) throws IOException {
	// 	String originalFileName = multipartFile.getOriginalFilename();
	// 	String convertedFileName;
	// 	String imageUrl = "";
	// 	final String path = bucket.concat("/static");
	//
	// 	if (originalFileName != null) {
	// 		convertedFileName = convertFileNameToUuid(originalFileName, userId);
	// 		ObjectMetadata metadata = new ObjectMetadata();
	// 		metadata.setContentLength(multipartFile.getSize());
	// 		metadata.setContentType(multipartFile.getContentType());
	//
	// 		amazonS3Client.putObject(path, convertedFileName, multipartFile.getInputStream(), metadata);
	// 		imageUrl = amazonS3Client.getUrl(path, convertedFileName).toString();
	// 	}
	// 	return imageUrl;
	// }

	// public String uploadCommunityFiles(MultipartFile multipartFile) throws IOException {
	// 	String originalFileName = multipartFile.getOriginalFilename();
	// 	String convertedFileName;
	// 	String imageUrl = "";
	// 	final String path = bucket.concat("/university_logo");
	//
	// 	if (originalFileName != null) {
	// 		convertedFileName = convertFileNameToUuid2(originalFileName);
	// 		ObjectMetadata metadata = new ObjectMetadata();
	// 		metadata.setContentLength(multipartFile.getSize());
	// 		metadata.setContentType(multipartFile.getContentType());
	//
	// 		amazonS3Client.putObject(path, convertedFileName, multipartFile.getInputStream(), metadata);
	// 		imageUrl = amazonS3Client.getUrl(path, convertedFileName).toString();
	// 	}
	//
	// 	return imageUrl;
	// }

	public String uploadFile(MultipartFile multipartFile, String bucketPath,
		String convertedFileName) throws IOException {
		String imageUrl = "";
		final String path = bucket.concat(bucketPath);

		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(multipartFile.getSize());
		metadata.setContentType(multipartFile.getContentType());

		amazonS3Client.putObject(path, convertedFileName, multipartFile.getInputStream(), metadata);
		imageUrl = amazonS3Client.getUrl(path, convertedFileName).toString();

		return imageUrl;
	}

	public String uploadFiles(MultipartFile multipartFile, Long userId) throws IOException {
		String originalFileName = multipartFile.getOriginalFilename();
		String convertedFileName = convertFileNameToUuid(originalFileName, userId);
		return uploadFile(multipartFile, "/user_profile_image_resized", convertedFileName);
	}

	public String uploadCommunityFiles(MultipartFile multipartFile) throws IOException {
		String originalFileName = multipartFile.getOriginalFilename();
		String convertedFileName = convertFileNameToUuid2(originalFileName);
		return uploadFile(multipartFile, "/community_profile_image_resized", convertedFileName);
	}

	private String convertFileNameToUuid(String fileName, Long userId) {
		String fileExtension = fileName.substring(fileName.lastIndexOf("."));
		return UUID.randomUUID().toString().concat("###" + userId.toString()).concat(fileExtension);
	}

	private String convertFileNameToUuid2(String fileName) {
		String fileExtension = fileName.substring(fileName.lastIndexOf("."));
		return UUID.randomUUID().toString().concat(fileExtension);
	}

}
