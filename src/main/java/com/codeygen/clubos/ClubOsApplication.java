package com.codeygen.clubos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ClubOsApplication {

	public static void main(String[] args) {
		System.out.println(System.getenv("DATABASE_USERNAME"));
		SpringApplication.run(ClubOsApplication.class, args);
	}

}
