package com.immo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ImmoProjectApplication {

	public static void main(String[] args) {
		SpringApplication.run(ImmoProjectApplication.class, args);
	}
}
