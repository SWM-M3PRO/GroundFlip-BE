package com.m3pro.groundflip.config;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import jakarta.annotation.PostConstruct;

@Configuration
public class FirebaseConfig {
	private FirebaseApp firebaseApp;
	@Value("${spring.firebase.secret}")
	private String secretKey;

	@PostConstruct
	public void init() throws IOException {
		byte[] decodedBytes = Base64.getDecoder().decode(secretKey);
		FirebaseOptions options = FirebaseOptions.builder()
			.setCredentials(GoogleCredentials.fromStream(new ByteArrayInputStream(decodedBytes)))
			.build();
		firebaseApp = FirebaseApp.initializeApp(options);
	}

	@Bean
	public FirebaseMessaging firebaseMessaging() {
		return FirebaseMessaging.getInstance(firebaseApp);
	}
}
