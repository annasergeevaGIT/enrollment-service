package at.enrollment_service.seervice;

import at.enrollment_service.BaseIntegrationTest;
import at.enrollment_service.config.EnrollmentServiceProps;
import at.enrollment_service.dto.EnrollmentResponse;
import at.enrollment_service.dto.SortBy;
import at.enrollment_service.exception.EnrollmentServiceException;
import at.enrollment_service.model.CourseLineItem;
import at.enrollment_service.model.EnrollmentStatus;
import at.enrollment_service.service.CourseEnrollmentServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static at.enrollment_service.testdata.TestConstants.*;
import static at.enrollment_service.testdata.TestDataProvider.createEnrollmentRequest;
import static at.enrollment_service.testdata.TestDataProvider.existingItems;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CourseEnrollmentServiceImplTest extends BaseIntegrationTest {

    @Autowired
    private CourseEnrollmentServiceImpl courseEnrollmentService;

    @Autowired
    private EnrollmentServiceProps enrollmentProps;

    @Test
    void getEnrollmentsOfUser_returnsCorrectList() {
        List<EnrollmentResponse> enrollments =
                courseEnrollmentService.getEnrollmentsOfUser(
                        USERNAME_ONE, SortBy.DATE_ASC, 0, 10);

        assertThat(enrollments).hasSize(3);

        assertEnrolment(enrollments.get(0), ENROLLMENT_ONE_DATE);
        assertEnrolment(enrollments.get(1), ENROLLMENT_TWO_DATE);
        assertEnrolment(enrollments.get(2), ENROLLMENT_THREE_DATE);
    }

    @Test
    void getEnrollmentsOfUser_returnsEmptyListWhenUserHasNone() {
        List<EnrollmentResponse> enrollments =
                courseEnrollmentService.getEnrollmentsOfUser(
                        "UnknownUser", SortBy.DATE_DESC, 0, 10);

        assertThat(enrollments).isEmpty();
    }

    @Test
    void createEnrollment_returnsError_whenServiceUnavailable() {
        prepareStubForServiceUnavailable();

        assertThatThrownBy(() ->
                courseEnrollmentService.createEnrollment(
                        createEnrollmentRequest(), USERNAME_ONE))
                .isInstanceOf(EnrollmentServiceException.class);

        wiremock.verify(
                enrollmentProps.getRetryCount() + 1,
                postRequestedFor(urlEqualTo(COURSE_INFO_PATH))
        );
    }


    @Test
    void createEnrollment_returnsError_whenTimeout() {
        prepareStubForSuccessWithTimeout();

        assertThatThrownBy(() ->
                courseEnrollmentService.createEnrollment(
                        createEnrollmentRequest(), USERNAME_ONE))
                .isInstanceOf(EnrollmentServiceException.class);

        wiremock.verify(1, postRequestedFor(urlEqualTo(COURSE_INFO_PATH)));
    }

    @Test
    void createEnrollment_returnsError_whenSomeCoursesNotAvailable() {
        prepareStubForPartialSuccess();

        assertThatThrownBy(() ->
                courseEnrollmentService.createEnrollment(
                        createEnrollmentRequest(), USERNAME_ONE))
                .isInstanceOf(EnrollmentServiceException.class);

        wiremock.verify(1, postRequestedFor(urlEqualTo(COURSE_INFO_PATH)));
    }

    @Test
    void createEnrollment_createsEnrollmentWhenAllCoursesAvailable() {
        prepareStubForSuccess();

        var request = createEnrollmentRequest();
        var now = LocalDateTime.now().minusNanos(1_000);

        EnrollmentResponse response =
                courseEnrollmentService.createEnrollment(request, USERNAME_ONE);

        assertThat(response.getEnrollmentId()).isNotNull();
        assertThat(response.getStatus()).isEqualTo(EnrollmentStatus.NEW);
        assertThat(response.getCreatedAt()).isAfter(now);
        assertThat(response.getAddress()).isEqualTo(request.getAddress());
        assertThat(response.getTotalPrice()).isEqualTo(SUCCESS_TOTAL_PRICE);

        var sorted = new ArrayList<>(response.getCourseLineItems());
        sorted.sort(Comparator.comparing(CourseLineItem::getPrice));

        assertThat(sorted).map(CourseLineItem::getCourseName)
                .containsExactly(COURSE_ONE, COURSE_TWO, COURSE_THREE);

        assertThat(sorted).map(CourseLineItem::getLanguage)
                .containsExactly(
                        COURSE_CREATE_ONE_LANGUAGE,
                        COURSE_CREATE_TWO_LANGUAGE,
                        COURSE_CREATE_THREE_LANGUAGE
                );

        assertThat(sorted).map(CourseLineItem::getPrice)
                .containsExactly(
                        COURSE_CREATE_ONE_PRICE,
                        COURSE_CREATE_TWO_PRICE,
                        COURSE_CREATE_THREE_PRICE
                );

        wiremock.verify(1, postRequestedFor(urlEqualTo(COURSE_INFO_PATH)));
    }

    private void assertEnrolment(EnrollmentResponse enrollment, LocalDateTime createdAt) {
        assertThat(enrollment.getEnrollmentId()).isNotNull();
        assertThat(enrollment.getAddress().getCity()).isEqualTo(CITY_ONE);
        assertThat(enrollment.getAddress().getStreet()).isEqualTo(STREET_ONE);
        assertThat(enrollment.getStatus()).isEqualTo(EnrollmentStatus.NEW);
        assertThat(enrollment.getCreatedAt()).isEqualTo(createdAt);
        assertThat(enrollment.getCourseLineItems()).isEqualTo(existingItems());
    }
}
