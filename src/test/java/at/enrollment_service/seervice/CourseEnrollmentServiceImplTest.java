package at.enrollment_service.seervice;

import at.enrollment_service.BaseIntegrationTest;
import at.enrollment_service.dto.EnrollmentResponse;
import at.enrollment_service.dto.SortBy;
import at.enrollment_service.exception.EnrollmentServiceException;
import at.enrollment_service.model.CourseLineItem;
import at.enrollment_service.model.EnrollmentStatus;
import at.enrollment_service.service.CourseEnrollmentServiceImpl;
import at.enrollment_service.testdata.TestConstants;
import org.assertj.core.api.AssertionsForInterfaceTypes;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;

import static at.enrollment_service.testdata.TestConstants.*;
import static at.enrollment_service.testdata.TestDataProvider.createEnrollmentRequest;
import static at.enrollment_service.testdata.TestDataProvider.existingItems;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@EnableAutoConfiguration(exclude = {KafkaAutoConfiguration.class})
public class CourseEnrollmentServiceImplTest extends BaseIntegrationTest {

    @Autowired
    private CourseEnrollmentServiceImpl courseEnrollmentService;

    @Test
    void getEnrollmentsOfUser_returnsCorrectFluxWhenUserHasEnrollments() {
        Flux<EnrollmentResponse> enrollments = courseEnrollmentService.getEnrollmentsOfUser(USERNAME_ONE, SortBy.DATE_ASC, 0, 10);
        StepVerifier.create(enrollments)
                .expectNextMatches(enrollment -> assertEnrolment(enrollment, ENROLLMENT_ONE_DATE))
                .expectNextMatches(enrollment -> assertEnrolment(enrollment, ENROLLMENT_TWO_DATE))
                .expectNextMatches(enrollment -> assertEnrolment(enrollment, ENROLLMENT_THREE_DATE))
                .verifyComplete();
    }

    @Test
    void getEnrollmentsOfUser_returnsEmptyFluxWhenNoEnrollments() {
        Flux<EnrollmentResponse> enrollments = courseEnrollmentService.getEnrollmentsOfUser("Unknown", SortBy.DATE_DESC, 0, 100);
        StepVerifier.create(enrollments)
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void createEnrollment_returnsError_whenServiceNotAvailable() {
        prepareStubForServiceUnavailable();

        var createEnrollmentRequest = createEnrollmentRequest();
        Mono<EnrollmentResponse> enrollment = courseEnrollmentService.createEnrollment(createEnrollmentRequest, USERNAME_ONE);
        StepVerifier.create(enrollment)
                .expectError(EnrollmentServiceException.class)
                .verify();
        wiremock.verify(6, postRequestedFor(urlEqualTo(COURSE_INFO_PATH)));
    }

    @Test
    void createEnrollment_returnsError_whenTimeout() {
        prepareStubForSuccessWithTimeout();

        var createEnrollmentRequest = createEnrollmentRequest();
        Mono<EnrollmentResponse> enrollment = courseEnrollmentService.createEnrollment(createEnrollmentRequest, USERNAME_ONE);
        StepVerifier.create(enrollment)
                .expectError(EnrollmentServiceException.class)
                .verify();
        wiremock.verify(6, postRequestedFor(urlEqualTo(COURSE_INFO_PATH)));
    }

    @Test
    void createEnrollment_returnsError_whenSomeCoursesAreNotAvailable() {
        prepareStubForPartialSuccess();

        var createEnrollmentRequest = createEnrollmentRequest();
        Mono<EnrollmentResponse> enrollment = courseEnrollmentService.createEnrollment(createEnrollmentRequest, USERNAME_ONE);
        StepVerifier.create(enrollment)
                .expectError(EnrollmentServiceException.class)
                .verify();
        wiremock.verify(1, postRequestedFor(urlEqualTo(COURSE_INFO_PATH)));
    }

    @Test
    void createEnrollment_createsEnrollmentWhenAllCoursesAreAvailable() {
        prepareStubForSuccess();

        var request = createEnrollmentRequest();
        var now = LocalDateTime.now().minusNanos(1000);
        Mono<EnrollmentResponse> response = courseEnrollmentService.createEnrollment(request, USERNAME_ONE);
        StepVerifier.create(response)
                .expectNextMatches(enrollmentResponse -> {
                    assertThat(enrollmentResponse.getAddress()).isEqualTo(request.getAddress());
                    assertThat(enrollmentResponse.getTotalPrice()).isEqualTo(TestConstants.SUCCESS_TOTAL_PRICE);
                    AssertionsForInterfaceTypes.assertThat(enrollmentResponse.getStatus()).isEqualTo(EnrollmentStatus.NEW);
                    assertThat(enrollmentResponse.getCreatedAt()).isAfter(now);

                    var courseItems = new ArrayList<>(enrollmentResponse.getCourseLineItems());
                    courseItems.sort(Comparator.comparing(CourseLineItem::getPrice));

                    AssertionsForInterfaceTypes.assertThat(courseItems)
                            .map(CourseLineItem::getCourseName)
                            .containsExactly(COURSE_ONE, COURSE_TWO, COURSE_THREE);

                    AssertionsForInterfaceTypes.assertThat(courseItems)
                            .map(CourseLineItem::getLanguage)
                            .containsExactly(COURSE_CREATE_ONE_LANGUAGE, COURSE_CREATE_TWO_LANGUAGE, COURSE_CREATE_THREE_LANGUAGE);

                    AssertionsForInterfaceTypes.assertThat(courseItems)
                            .map(CourseLineItem::getPrice)
                            .containsExactly(COURSE_CREATE_ONE_PRICE, COURSE_CREATE_TWO_PRICE, COURSE_CREATE_THREE_PRICE);
                    return enrollmentResponse.getEnrollmentId() != null;
                })
                .verifyComplete();

        wiremock.verify(1, postRequestedFor(urlEqualTo(COURSE_INFO_PATH)));
    }

    private boolean assertEnrolment(EnrollmentResponse enrollment, LocalDateTime createdAt) {
        return enrollment.getEnrollmentId() != null &&
                enrollment.getAddress().getCity().equals(CITY_ONE) &&
                enrollment.getAddress().getStreet().equals(STREET_ONE) &&
                enrollment.getStatus().equals(EnrollmentStatus.NEW) &&
                enrollment.getCreatedAt().equals(createdAt) &&
                enrollment.getCourseLineItems().equals(existingItems());
    }
}
