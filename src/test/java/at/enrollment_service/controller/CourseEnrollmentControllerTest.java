package at.enrollment_service.controller;

import at.enrollment_service.BaseIntegrationTest;
import at.enrollment_service.dto.EnrollmentResponse;
import at.enrollment_service.model.EnrollmentStatus;
import org.assertj.core.api.AssertionsForInterfaceTypes;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Comparator;

import static at.enrollment_service.controller.CourseEnrollmentController.USER_HEADER;
import static at.enrollment_service.testdata.TestConstants.*;
import static at.enrollment_service.testdata.TestDataProvider.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@AutoConfigureWebTestClient(timeout = "20000") // Increase timeout for WebTestClient
public class CourseEnrollmentControllerTest extends BaseIntegrationTest {

    @Autowired
    protected WebTestClient webTestClient;

    @Test
    void submitCourseEnrollment_returnsCorrectResponse() {
        prepareStubForSuccess();
        var validRequest = createEnrollmentRequest();
        var expectedCourseItems = createdItems();
        webTestClient.post()
                .uri(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .header(USER_HEADER, USERNAME_ONE)
                .bodyValue(validRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(EnrollmentResponse.class)
                .value(response -> {
                    assertThat(response.getEnrollmentId()).isNotNull();
                    AssertionsForInterfaceTypes.assertThat(response.getCourseLineItems()).isEqualTo(expectedCourseItems);
                    AssertionsForInterfaceTypes.assertThat(response.getStatus()).isEqualTo(EnrollmentStatus.NEW);
                    assertThat(response.getTotalPrice()).isEqualTo(SUCCESS_TOTAL_PRICE);
                    assertThat(response.getAddress()).isEqualTo(validRequest.getAddress());
                });
    }

    @Test
    void submitCourseEnrollment_returnsNotFound_whenSomeCoursesAreNotAvailableInCourseService() {
        prepareStubForPartialSuccess();
        var request = createEnrollmentRequest();
        webTestClient.post()
                .uri(BASE_URL)
                .header(USER_HEADER, USERNAME_ONE)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getEnrollmentsOfUser_returnsCorrectlySortedListOfEnrollments() {
        webTestClient.get()
                .uri(BASE_URL + "?from=0&size=10&sortBy=date_asc")
                .header(USER_HEADER, USERNAME_ONE)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(EnrollmentResponse.class)
                .value(enrollments -> {
                    AssertionsForInterfaceTypes.assertThat(enrollments).hasSize(3)
                            .isSortedAccordingTo(Comparator.comparing(EnrollmentResponse::getCreatedAt));
                });
    }

    @Test
    void submitCourseEnrollment_returnsBadRequest_whenEnrollmentInvalid() {
        var invalidRequest = createEnrollmentInvalidRequest();
        webTestClient.post()
                .uri(BASE_URL)
                .header(USER_HEADER, USERNAME_ONE)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidRequest)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void submitCourseEnrollment_returnsServiceUnavailableOnTimeout() {
        prepareStubForSuccessWithTimeout();
        var validRequest = createEnrollmentRequest();
        webTestClient.post()
                .uri(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .header(USER_HEADER, USERNAME_ONE)
                .bodyValue(validRequest)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void getEnrollmentsOfUser_returnsBadRequestForInvalidParams() {
        webTestClient.get()
                .uri(BASE_URL + "?from=-1&size=10")
                .header(USER_HEADER, USERNAME_ONE)
                .exchange()
                .expectStatus().isBadRequest();
    }
}
