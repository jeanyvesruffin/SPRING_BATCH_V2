package com.ruffin.SPRING_BATCH_V2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.ruffin.SPRING_BATCH_V2.config.ApplicationProperties;

@SpringBootApplication
@EnableConfigurationProperties({LiquibaseProperties.class, ApplicationProperties.class})
public class ClientBatchLoaderApp {

	public static void main(String[] args) {
		SpringApplication.run(ClientBatchLoaderApp.class, args);
	}

}
