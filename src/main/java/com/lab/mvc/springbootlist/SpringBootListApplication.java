package com.lab.mvc.springbootlist;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Bootstraps Spring and discovers application components in this package tree. */
@SpringBootApplication
public class SpringBootListApplication {

	/** Starts the embedded web server and the Spring application context. */
	public static void main(String[] args) {
		SpringApplication.run(SpringBootListApplication.class, args);
	}

}
