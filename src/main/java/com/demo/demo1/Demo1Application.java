package com.demo.demo1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Demo1Application {
	public static Logger logger = LoggerFactory.getLogger(Demo1Application.class);
	//加行注释1
	public static void main(String[] args) {
		SpringApplication.run(Demo1Application.class, args);
		logger.info("Demo1Application started...");
	}
}
