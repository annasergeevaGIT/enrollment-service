package at.enrollment_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableDiscoveryClient
@SpringBootApplication // scan for @Component, @Service, @Repository, @Controller, @RestController, etc.
@ConfigurationPropertiesScan // scan @ConfigurationProperties classes
@EnableScheduling
@EnableRetry
public class EnrollmentServiceApplication { public static void main(String[] args) {
		SpringApplication.run(EnrollmentServiceApplication.class, args);
	}
}
