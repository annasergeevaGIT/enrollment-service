package at.enrollment_service.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/*
* basic configuration of WebClient for communication with the Course Service.
 */
@RequiredArgsConstructor
@Configuration
public class WebClientConfig {
    private final EnrollmentServiceProps enrollmentServiceProps;

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl(enrollmentServiceProps.getCourseServiceUrl())
                .build();
    }
}
