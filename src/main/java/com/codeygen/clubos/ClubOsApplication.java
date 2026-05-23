package com.codeygen.clubos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ClubOsApplication {

	public static void main(String[] args) {
		System.out.println(System.getenv("DATABASE_USERNAME"));
		SpringApplication.run(ClubOsApplication.class, args);
	}

}
