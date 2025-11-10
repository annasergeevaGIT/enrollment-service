package at.enrollment_service.config;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/*
* basic configuration of WebClient for communication with the Course Service.
 */
@RequiredArgsConstructor
@Configuration
public class WebClientConfig {

    private final EnrollmentServiceProps props;

    @LoadBalanced
    @Bean
    public WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    public WebClient webClient(WebClient.Builder loadBalancedWebClientBuilder) {
        return loadBalancedWebClientBuilder
                .baseUrl(props.getCourseServiceUrl())
                .build();
    }
}
