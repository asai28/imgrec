package com.cloudcomputing.cse546.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.regions.Regions;

@Configuration
@EnableAutoConfiguration
@ComponentScan({"classes","com.cloudcomputing.cse546.app","com.cloudcomputing.cse546.controllers"})
public class ImageRecognitionApp {
	
	public static void main(String[] args) {
		SpringApplication.run(ImageRecognitionApp.class, args);
	}
}
