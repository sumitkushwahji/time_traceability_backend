package com.time.tracealibility;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class TracealibilityApplication {

	public static void main(String[] args) {
		SpringApplication.run(TracealibilityApplication.class, args);
	}

}
