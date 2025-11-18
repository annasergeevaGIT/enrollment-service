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
import static org.junit.jupiter.api.Assertions.assertThrows;

@EnableAutoConfiguration(exclude = {KafkaAutoConfiguration.class})
public class CourseEnrollmentServiceImplTest extends BaseIntegrationTest {

    @Autowired
    private CourseEnrollmentServiceImpl courseEnrollmentService;

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
        assertThrows(EnrollmentServiceException.class, () -> { // Use blocking assertThrows
            courseEnrollmentService.createEnrollment(createEnrollmentRequest, USERNAME_ONE);
        });
        wiremock.verify(RETRY_COUNT + 1, postRequestedFor(urlEqualTo(COURSE_INFO_PATH))); // 3 retries + 1 initial attempt = 4 total requests
    }

    @Test
    void createEnrollment_returnsError_whenTimeout() {
        prepareStubForSuccessWithTimeout();
        var createEnrollmentRequest = createEnrollmentRequest();
        assertThrows(EnrollmentServiceException.class, () -> {
            courseEnrollmentService.createEnrollment(createEnrollmentRequest, USERNAME_ONE);
        });
        wiremock.verify(RETRY_COUNT + 1, postRequestedFor(urlEqualTo(COURSE_INFO_PATH))); // 3 retries + 1 initial attempt = 4 total requests
    }

    @Test
    void createEnrollment_returnsError_whenSomeCoursesAreNotAvailable() {
        prepareStubForPartialSuccess();
        var createEnrollmentRequest = createEnrollmentRequest();

        // CHANGE: Use blocking assertThrows
        assertThrows(EnrollmentServiceException.class, () -> {
            courseEnrollmentService.createEnrollment(createEnrollmentRequest, USERNAME_ONE);
        });

        wiremock.verify(1, postRequestedFor(urlEqualTo(COURSE_INFO_PATH)));
    }

    @Test
    void createEnrollment_createsEnrollmentWhenAllCoursesAreAvailable() {
        prepareStubForSuccess();

        var request = createEnrollmentRequest();
        var now = LocalDateTime.now().minusNanos(1000);

        EnrollmentResponse response = courseEnrollmentService.createEnrollment(request, USERNAME_ONE);

        assertThat(response.getAddress()).isEqualTo(request.getAddress());
        assertThat(response.getTotalPrice()).isEqualTo(TestConstants.SUCCESS_TOTAL_PRICE);
        AssertionsForInterfaceTypes.assertThat(response.getStatus()).isEqualTo(EnrollmentStatus.NEW);
        assertThat(response.getCreatedAt()).isAfter(now);
        assertThat(response.getEnrollmentId()).isNotNull();

        var courseItems = new ArrayList<>(response.getCourseLineItems());
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
