package com.QueroTrabalhar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class QueroTrabalharApplication {

	public static void main(String[] args) {
		SpringApplication.run(QueroTrabalharApplication.class, args);
	}

}
