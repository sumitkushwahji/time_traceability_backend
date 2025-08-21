package com.time.tracealibility;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@EnableScheduling
@SpringBootApplication
public class TracealibilityApplication {

	public static void main(String[] args) {
		// Set timezone to UTC to avoid PostgreSQL timezone issues
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		SpringApplication.run(TracealibilityApplication.class, args);
	}

}
