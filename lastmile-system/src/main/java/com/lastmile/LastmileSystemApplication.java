package com.lastmile;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LastmileSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(LastmileSystemApplication.class, args);
	}

}
