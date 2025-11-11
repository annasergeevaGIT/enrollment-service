package at.enrollment_service.config;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerExchangeFilterFunction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.reactive.function.client.WebClient;

/*
* basic configuration of WebClient for communication with the Course Service.
 */
@RequiredArgsConstructor
@Configuration
@Profile("!test") // disable load balancer in tests
public class WebClientConfig {

    private final EnrollmentServiceProps props;
    private final ReactorLoadBalancerExchangeFilterFunction lbFunction;

    @Bean
    public WebClient webClient(WebClient.Builder builder) { //reactive filter https://docs.spring.io/spring-cloud-commons/reference/spring-cloud-commons/common-abstractions.html#webflux-with-reactive-loadbalancer
        return builder
                .filter(lbFunction)
                .baseUrl(props.getCourseServiceUrl())
                .build();
    }
}
