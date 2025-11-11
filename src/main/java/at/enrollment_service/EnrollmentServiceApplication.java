package at.enrollment_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import reactor.core.publisher.Hooks;

@SpringBootApplication // scan for @Component, @Service, @Repository, @Controller, @RestController, etc.
@ConfigurationPropertiesScan // scan @ConfigurationProperties classes
public class EnrollmentServiceApplication {

	public static void main(String[] args) {
		Hooks.enableAutomaticContextPropagation();
		SpringApplication.run(EnrollmentServiceApplication.class, args);
	}

}
