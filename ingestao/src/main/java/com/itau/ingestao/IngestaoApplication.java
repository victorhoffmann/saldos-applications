package com.itau.ingestao;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class IngestaoApplication {

	public static void main(String[] args) {
		SpringApplication.run(IngestaoApplication.class, args);
	}

}
