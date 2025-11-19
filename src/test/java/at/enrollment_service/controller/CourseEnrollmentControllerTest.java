package at.enrollment_service.controller;

import at.enrollment_service.BaseTest;
import at.enrollment_service.dto.EnrollmentResponse;
import at.enrollment_service.model.EnrollmentStatus;
import at.enrollment_service.testdata.TestConstants;
import at.enrollment_service.testdata.TestDataProvider;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CourseEnrollmentControllerTest extends BaseTest {

    @Autowired
    private WebTestClient webTestClient;

    @RegisterExtension
    static WireMockExtension wiremock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @DynamicPropertySource
    static void applyProperties(DynamicPropertyRegistry registry) {
        registry.add("external.course-service-url", wiremock::baseUrl);
    }

    @Test
    void submitCourseEnrollment_returnsCorrectResponse() {
        wiremock.stubFor(post("/v1/courses/course-info")
                .willReturn(okJson(TestDataProvider.readSuccessfulResponse())));

        var validRequest = TestDataProvider.createEnrollmentRequest();

        webTestClient.post()
                .uri("/v1/course-enrollments")
                .header(CourseEnrollmentController.USER_HEADER, "username1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(EnrollmentResponse.class)
                .value(response -> {
                    assertThat(response.getEnrollmentId()).isNotNull();
                    assertThat(response.getStatus()).isEqualTo(EnrollmentStatus.NEW);
                    assertThat(response.getTotalPrice()).isEqualByComparingTo(TestConstants.SUCCESS_TOTAL_PRICE);
                });
    }

    @Test
    void submitCourseEnrollment_returns503_whenServiceUnavailable() {
        wiremock.stubFor(post("/v1/courses/course-info").willReturn(serviceUnavailable()));

        var validRequest = TestDataProvider.createEnrollmentRequest();

        webTestClient.post()
                .uri("/v1/course-enrollments")
                .header(CourseEnrollmentController.USER_HEADER, "username1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validRequest)
                .exchange()
                .expectStatus().isEqualTo(503);
    }
}