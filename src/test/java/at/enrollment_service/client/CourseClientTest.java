package at.enrollment_service.client;

import at.enrollment_service.config.EnrollmentServiceProps;
import at.enrollment_service.dto.CourseInfo;
import at.enrollment_service.dto.GetCourseInfoRequest;
import at.enrollment_service.dto.GetCourseInfoResponse;
import at.enrollment_service.exception.EnrollmentServiceException;
import at.enrollment_service.mapper.CourseClient;
import at.enrollment_service.testdata.TestDataProvider;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.net.http.HttpClient;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static at.enrollment_service.testdata.TestConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CourseClientTest {

    private final EnrollmentServiceProps props = new EnrollmentServiceProps(
            "http://localhost:8081",
            "/api/courses/enrollment-info",
            DEFAULT_TIMEOUT,
            RETRY_BACKOFF,
            RETRY_COUNT,
            RETRY_JITTER
    );

    private CourseClient courseClient;
    private MockWebServer mockWebServer;

    @BeforeEach
    void setupServer() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(props.getDefaultTimeout())
                .build();

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(props.getDefaultTimeout());

        RestClient restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .baseUrl(mockWebServer.url("/").toString())
                .build();

        courseClient = new CourseClient(restClient, props);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockWebServer.shutdown();
    }

    @Test
    void getCourseInfo_returnsError_whenTimeout() throws Exception {
        mockWebServer.enqueue(
                TestDataProvider.partialSuccessResponse()
                        .setBodyDelay(DELAY_MILLIS, TimeUnit.MILLISECONDS)
        );

        GetCourseInfoRequest request =
                new GetCourseInfoRequest(Set.of("One", "Two", "Three"));

        assertThatThrownBy(() -> courseClient.getCourseInfo(request))
                .isInstanceOf(EnrollmentServiceException.class);

        verifyNumberOfPostRequests(4);
    }

    @Test
    void getCourseInfo_returnsInfo_whenRetriesSucceed() throws Exception {
        mockWebServer.enqueue(new MockResponse().setResponseCode(503));
        mockWebServer.enqueue(
                TestDataProvider.partialSuccessResponse()
                        .setBodyDelay(DELAY_MILLIS, TimeUnit.MILLISECONDS)
        );
        mockWebServer.enqueue(TestDataProvider.partialSuccessResponse());

        GetCourseInfoRequest request =
                new GetCourseInfoRequest(Set.of("One", "Two", "Three"));

        GetCourseInfoResponse response = courseClient.getCourseInfo(request);

        assertResponseCorrect(response);
        verifyNumberOfPostRequests(3);
    }

    @Test
    void getCourseInfo_returnsErrorWhenServiceUnavailableAndAllRetriesExhausted()
            throws Exception {

        for (int i = 0; i < 4; i++) {
            mockWebServer.enqueue(new MockResponse().setResponseCode(503));
        }

        GetCourseInfoRequest request =
                new GetCourseInfoRequest(Set.of("One", "Two", "Three"));

        assertThatThrownBy(() -> courseClient.getCourseInfo(request))
                .isInstanceOf(EnrollmentServiceException.class);

        verifyNumberOfPostRequests(4);
    }

    @Test
    void getCourseInfo_returnsInfo_whenAllIsOk() throws Exception {
        mockWebServer.enqueue(TestDataProvider.partialSuccessResponse());

        GetCourseInfoRequest request =
                new GetCourseInfoRequest(Set.of("One", "Two", "Three"));

        GetCourseInfoResponse response = courseClient.getCourseInfo(request);

        assertResponseCorrect(response);
        verifyNumberOfPostRequests(1);
    }

    private void assertResponseCorrect(GetCourseInfoResponse response) {
        List<CourseInfo> infos = new ArrayList<>(response.getCourseInfos());
        infos.sort(Comparator.comparing(CourseInfo::getName));

        assertThat(infos)
                .map(CourseInfo::getName)
                .containsExactly("One", "Three", "Two");

        assertThat(infos)
                .map(CourseInfo::getPrice)
                .containsExactly(
                        BigDecimal.valueOf(10.1),
                        BigDecimal.valueOf(30.3),
                        null
                );

        assertThat(infos)
                .map(CourseInfo::getIsAvailable)
                .containsExactly(true, true, false);
    }

    private void verifyNumberOfPostRequests(int expected) throws Exception {
        for (int i = 0; i < expected; i++) {
            RecordedRequest req = mockWebServer.takeRequest(5, TimeUnit.SECONDS);

            assertThat(req).as("Request %s missing", i).isNotNull();
            assertThat(req.getMethod()).isEqualTo("POST");
            assertThat(req.getPath()).isEqualTo(props.getCourseInfoPath());
        }

        // Ensure no unexpected extra requests
        assertThat(mockWebServer.takeRequest(200, TimeUnit.MILLISECONDS)).isNull();
    }
}
