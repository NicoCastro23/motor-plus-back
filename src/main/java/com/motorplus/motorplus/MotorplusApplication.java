package com.motorplus.motorplus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MotorplusApplication {

	public static void main(String[] args) {
		SpringApplication.run(MotorplusApplication.class, args);
	}

}
