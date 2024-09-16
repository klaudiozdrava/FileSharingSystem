package com.kzdrava.webapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = {"org.unipi.repositories", "com.kzdrava.webapp.auth"})
@ComponentScan(basePackages = { "org.unipi.repositories", "com.kzdrava.webapp" })
@EntityScan(basePackages = {"org.unipi", "com.kzdrava.webapp.entities"})
public class WebAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebAppApplication.class, args);
	}

}
