package at.enrollment_service.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerExchangeFilterFunction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;

//basic configuration of WebClient for communication with the Course Service.
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(EnrollmentServiceProps.class)
public class RestClientConfig {

    private final EnrollmentServiceProps props;

    @Bean
    @LoadBalanced
    public RestClient.Builder loadBalancedRestClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    public RestClient restClient(RestClient.Builder builder) {

        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(props.getDefaultTimeout()) //connect timeout
                .build();

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(props.getDefaultTimeout()); //read timeout

        return builder
                .requestFactory(requestFactory)
                .baseUrl("http://course-service")   // Eureka serviceId
                .build();
    }
}