package at.enrollment_service.service;

import at.enrollment_service.BaseIntegrationTest;
import at.enrollment_service.dto.EnrollmentResponse;
import at.enrollment_service.dto.SortBy;
import at.enrollment_service.exception.EnrollmentServiceException;
import at.enrollment_service.model.CourseLineItem;
import at.enrollment_service.model.EnrollmentStatus;
import at.enrollment_service.testdata.TestConstants;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static at.enrollment_service.testdata.TestConstants.*;
import static at.enrollment_service.testdata.TestDataProvider.createEnrollmentRequest;
import static at.enrollment_service.testdata.TestDataProvider.existingItems;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.Assert.assertThrows;


@EnableAutoConfiguration(exclude = {KafkaAutoConfiguration.class})
public class CourseEnrollmentServiceImplTest extends BaseIntegrationTest {

    @Autowired
    private CourseEnrollmentService courseEnrollmentService;

    @Test
    void getEnrollmentsOfUser_returnsCorrectListWhenUserHasEnrollments() {
        List<EnrollmentResponse> enrollments = courseEnrollmentService.getEnrollmentsOfUser(USERNAME_ONE, SortBy.DATE_ASC, 0, 10);
        assertThat(enrollments).hasSize(3);
        assertThat(assertEnrolment(enrollments.get(0), ENROLLMENT_ONE_DATE)).isTrue();
        assertThat(assertEnrolment(enrollments.get(1), ENROLLMENT_TWO_DATE)).isTrue();
        assertThat(assertEnrolment(enrollments.get(2), ENROLLMENT_THREE_DATE)).isTrue();
    }

    @Test
    void getEnrollmentsOfUser_returnsEmptyListWhenNoEnrollments() {
        List<EnrollmentResponse> enrollments = courseEnrollmentService.getEnrollmentsOfUser("Unknown", SortBy.DATE_DESC, 0, 100);
        assertThat(enrollments).isEmpty();
    }

    @Test
    void createEnrollment_returnsError_whenServiceNotAvailable() {
        prepareStubForServiceUnavailable();
        var createEnrollmentRequest = createEnrollmentRequest();
        assertThrows(EnrollmentServiceException.class, () -> {
            courseEnrollmentService.createEnrollment(createEnrollmentRequest, USERNAME_ONE);
        });

        wiremock.verify(5, postRequestedFor(urlEqualTo(COURSE_INFO_PATH)));
    }

    @Test
    void createEnrollment_returnsError_whenTimeout() {
        prepareStubForSuccessWithTimeout();
        var createEnrollmentRequest = createEnrollmentRequest();

        assertThrows(EnrollmentServiceException.class, () -> {
            courseEnrollmentService.createEnrollment(createEnrollmentRequest, USERNAME_ONE);
        });

        // Verifies retries happened on timeout
        wiremock.verify(5, postRequestedFor(urlEqualTo(COURSE_INFO_PATH)));
    }

    @Test
    void createEnrollment_returnsError_whenSomeCoursesAreNotAvailable() {
        prepareStubForPartialSuccess();
        var createEnrollmentRequest = createEnrollmentRequest();

        assertThrows(EnrollmentServiceException.class, () -> {
            courseEnrollmentService.createEnrollment(createEnrollmentRequest, USERNAME_ONE);
        });
        wiremock.verify(1, postRequestedFor(urlEqualTo(COURSE_INFO_PATH)));
    }

    @Test
    void createEnrollment_createsEnrollmentWhenAllCoursesAreAvailable() {
        prepareStubForSuccess();

        var request = createEnrollmentRequest();
        var now = LocalDateTime.now().minusSeconds(1); // Slight buffer
        EnrollmentResponse response = courseEnrollmentService.createEnrollment(request, USERNAME_ONE);
        assertThat(response.getAddress()).isEqualTo(request.getAddress());
        assertThat(response.getTotalPrice()).isEqualByComparingTo(TestConstants.SUCCESS_TOTAL_PRICE); // Better for BigDecimal
        assertThat(response.getStatus()).isEqualTo(EnrollmentStatus.NEW);
        assertThat(response.getCreatedAt()).isAfter(now);
        assertThat(response.getEnrollmentId()).isNotNull();

        var courseItems = new ArrayList<>(response.getCourseLineItems());
        courseItems.sort(Comparator.comparing(CourseLineItem::getPrice));
        assertThat(courseItems)
                .map(CourseLineItem::getCourseName)
                .containsExactly(COURSE_ONE, COURSE_TWO, COURSE_THREE);
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