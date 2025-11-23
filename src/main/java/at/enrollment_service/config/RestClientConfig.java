package at.enrollment_service.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;

//basic configuration of WebClient for communication with the Course Service.
@RequiredArgsConstructor
@Configuration
@EnableConfigurationProperties(EnrollmentServiceProps.class)
public class RestClientConfig {

    private final EnrollmentServiceProps props;

    @Bean
    public RestClient restClient(RestClient.Builder builder) {

        // Force HTTP/1.1 to avoid intermittent HTTP/2 RST_STREAM issues with WireMock/JDK client.
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(props.getDefaultTimeout())
                .build();

        var requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(props.getDefaultTimeout());

        return builder
                .requestFactory(requestFactory)
                .baseUrl(props.getCourseServiceUrl())
                .build();
    }
}