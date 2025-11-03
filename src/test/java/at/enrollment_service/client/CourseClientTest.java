package at.enrollment_service.client;

import at.enrollment_service.config.EnrollmentServiceProps;
import at.enrollment_service.dto.CourseInfo;
import at.enrollment_service.dto.GetCourseInfoRequest;
import at.enrollment_service.dto.GetCourseInfoResponse;
import at.enrollment_service.exception.EnrollmentServiceException;
import at.enrollment_service.model.mapper.CourseClient;
import at.enrollment_service.testdata.TestDataProvider;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.assertj.core.api.AssertionsForInterfaceTypes;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static at.enrollment_service.testdata.TestConstants.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

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
        mockWebServer = new MockWebServer(); // local in memory HTTP server
        mockWebServer.start();
        var webClient = WebClient.builder() //route client to base url of mockWebServer
                .baseUrl(mockWebServer.url("/").uri().toString())
                .build(); //directs all WebClient calls to the mock server instead of the real service
        courseClient = new CourseClient(webClient, props); //Instance client under test. injects the test WebClient and fake props to test its behavior.
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void getCourseInfo_returnsError_whenTimeout() throws Exception { //Simulates the Course Service taking too long to respond
        mockWebServer.enqueue(TestDataProvider.partialSuccessResponse().setBodyDelay(DELAY_MILLIS, TimeUnit.MILLISECONDS));

        var request = new GetCourseInfoRequest(Set.of("One", "Two", "Three"));
        Mono<GetCourseInfoResponse> response = courseClient.getCourseInfo(request); //Sends the request via reactive WebClient to the mock server.

        StepVerifier.create(response) //when the response takes too long, the client throws an EnrollmentServiceException
                .expectError(EnrollmentServiceException.class)
                .verify();
        verifyNumberOfPostRequests(4); // client retried 3 times + 1 initial attempt before failing
    }

    @Test
    void getCourseInfo_returnsInfo_whenRetriesSucceed() throws Exception {
        // on first request server responds with SERVICE_UNAVAILABLE
        mockWebServer.enqueue(new MockResponse().setResponseCode(HttpStatus.SERVICE_UNAVAILABLE.value()));
        // on second request server responds with a delay (1500ms)
        mockWebServer.enqueue(TestDataProvider.partialSuccessResponse().setBodyDelay(DELAY_MILLIS, TimeUnit.MILLISECONDS));
        // on third request server responds without delay
        mockWebServer.enqueue(TestDataProvider.partialSuccessResponse());

        var request = new GetCourseInfoRequest(Set.of("One", "Two", "Three"));
        Mono<GetCourseInfoResponse> response = courseClient.getCourseInfo(request);
        assertResponseCorrect(response);
        verifyNumberOfPostRequests(3);
    }

    @Test
    void getCourseInfo_returnsErrorWhenServiceUnavailableAndAllRetriesExhausted() throws Exception {
        mockWebServer.enqueue(new MockResponse().setResponseCode(HttpStatus.SERVICE_UNAVAILABLE.value()));
        var request = new GetCourseInfoRequest(Set.of("One", "Two", "Three"));
        Mono<GetCourseInfoResponse> response = courseClient.getCourseInfo(request);

        StepVerifier.create(response)
                .expectError(EnrollmentServiceException.class)
                .verify();
        verifyNumberOfPostRequests(4);
    }

    @Test
    void getCourseInfo_returnsInfo_whenAllIsOk() throws Exception {
        mockWebServer.enqueue(TestDataProvider.partialSuccessResponse());
        var request = new GetCourseInfoRequest(Set.of("One", "Two", "Three"));
        Mono<GetCourseInfoResponse> response = courseClient.getCourseInfo(request);
        assertResponseCorrect(response);
        verifyNumberOfPostRequests(1);
    }

    private void assertResponseCorrect(Mono<GetCourseInfoResponse> response) {
        StepVerifier.create(response)
                .expectNextMatches(result -> {
                    List<CourseInfo> courseInfos = result.getCourseInfos();
                    courseInfos.sort(Comparator.comparing(CourseInfo::getName));
                    AssertionsForInterfaceTypes.assertThat(courseInfos)
                            .map(CourseInfo::getName)
                            .containsExactly("One", "Three", "Two");
                    AssertionsForInterfaceTypes.assertThat(courseInfos)
                            .map(CourseInfo::getPrice)
                            .containsExactly(
                                    BigDecimal.valueOf(10.1),
                                    BigDecimal.valueOf(30.3),
                                    null
                            );
                    AssertionsForInterfaceTypes.assertThat(courseInfos)
                            .map(CourseInfo::getIsAvailable)
                            .containsExactly(
                                    true, true, false
                            );
                    return true;
                })
                .verifyComplete();
    }


    private void verifyNumberOfPostRequests(int times) throws Exception {
        for (int i = 0; i < times; i++) {
            // Asserts that the expected number of POST requests were made to the mock server.
            // make a timeout so that test does not hang indefinitely
            RecordedRequest recordedRequest = mockWebServer.takeRequest(1000, TimeUnit.MILLISECONDS);
            assertThat(recordedRequest)
                    .as("Recorded requests: %d, expected: %d", i, times)
                    .isNotNull();
            assertThat(recordedRequest.getMethod()).isEqualTo("POST");
            assertThat(recordedRequest.getPath()).isEqualTo(props.getCourseInfoPath());
        }
        assertThat(mockWebServer.takeRequest(1000, TimeUnit.MILLISECONDS))
                .as("Expected %d requests, but received more", times).isNull();
    }

}
