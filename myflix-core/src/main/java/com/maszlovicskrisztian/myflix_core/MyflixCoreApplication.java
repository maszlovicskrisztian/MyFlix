package com.maszlovicskrisztian.myflix_core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MyflixCoreApplication {

	public static void main(String[] args) {
		SpringApplication.run(MyflixCoreApplication.class, args);
	}

}
