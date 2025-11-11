package at.enrollment_service;

import at.enrollment_service.config.EnrollmentServiceProps;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

@TestConfiguration
public class TestWebClientConfig {

    private final EnrollmentServiceProps props;

    public TestWebClientConfig(EnrollmentServiceProps props) {
        this.props = props;
    }

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder.baseUrl(props.getCourseServiceUrl()).build();
    }
}
