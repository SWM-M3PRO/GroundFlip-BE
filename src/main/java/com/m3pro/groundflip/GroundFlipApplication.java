package com.m3pro.groundflip;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class GroundFlipApplication {

	public static void main(String[] args) {
		SpringApplication.run(GroundFlipApplication.class, args);
	}

}
