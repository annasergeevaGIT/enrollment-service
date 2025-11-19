package at.enrollment_service.client;

import at.enrollment_service.config.EnrollmentServiceProps;
import at.enrollment_service.dto.GetCourseInfoRequest;
import at.enrollment_service.mapper.CourseClient;
import at.enrollment_service.testdata.TestDataProvider;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CourseClientTest {

    private MockWebServer mockWebServer;
    private CourseClient courseClient;

    @BeforeEach
    void setup() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        var props = new EnrollmentServiceProps(
                mockWebServer.url("/").toString(), // Base URL
                "", // path
                java.time.Duration.ofSeconds(1),
                java.time.Duration.ofMillis(10),
                3,
                0.5
        );
        courseClient = new CourseClient(RestClient.builder().requestFactory(new HttpComponentsClientHttpRequestFactory()), props);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockWebServer.shutdown();
    }

    @Test
    void getCourseInfo_returnsInfo_whenAllIsOk() throws Exception {
        mockWebServer.enqueue(TestDataProvider.successResponse());

        var request = new GetCourseInfoRequest(Set.of("One", "Two", "Three"));
        var response = courseClient.getCourseInfo(request);

        assertThat(response.getCourseInfos()).hasSize(3);
        assertThat(response.getCourseInfos().getFirst().getName()).isEqualTo("One");

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo("POST");
    }
}