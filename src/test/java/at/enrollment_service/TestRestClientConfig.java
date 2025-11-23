package at.enrollment_service;

import at.enrollment_service.config.EnrollmentServiceProps;
import at.enrollment_service.config.RestClientConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;

@TestConfiguration
public class TestRestClientConfig {

    @Bean
    @Primary
    public RestClient restClient(RestClient.Builder builder, EnrollmentServiceProps props) {

        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)   // MUST KEEP
                .connectTimeout(props.getDefaultTimeout())
                .build();

        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(props.getDefaultTimeout());

        return builder
                .requestFactory(factory)
                .baseUrl(props.getCourseServiceUrl())
                .build();
    }
}