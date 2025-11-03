package at.enrollment_service.repository;

import at.enrollment_service.BaseTest;
import at.enrollment_service.config.R2dbcConfig;
import at.enrollment_service.model.CourseEnrollment;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static at.enrollment_service.testdata.TestConstants.*;
import static at.enrollment_service.testdata.TestConstants.ENROLLMENT_TWO_DATE;

@Import({R2dbcConfig.class}) // import custom converters configuration
@ImportAutoConfiguration({JacksonAutoConfiguration.class}) //ensure Jackson config is loaded
@DataR2dbcTest //slice Test loads R2DBC components, rolls back after each test
public class CourseEnrollmentRepositoryTest extends BaseTest {

    @Autowired
    private CourseEnrollmentReopsitory repository;

    @Test
    void findAllByCreatedBy_returnsCorrectSortedByDateDesc() {
        var pageRequest = PageRequest.of(0, 2)
                .withSort(Sort.by(Sort.Direction.DESC, "createdAt"));
        Flux<CourseEnrollment> enrollments = repository.findAllByCreatedBy(USERNAME_ONE, pageRequest);
        StepVerifier.create(enrollments)
                .expectNextMatches(enrollment ->
                        enrollment.getCreatedBy().equals(USERNAME_ONE) &&
                                enrollment.getCreatedAt().equals(ENROLLMENT_THREE_DATE))
                .expectNextMatches(enrollment ->
                        enrollment.getCreatedBy().equals(USERNAME_ONE) &&
                                enrollment.getCreatedAt().equals(ENROLLMENT_TWO_DATE) &&
                                enrollment.getUpdatedAt() != null)
                .verifyComplete();
    }

    @Test
    void findAllByCreatedBy_returnsCorrectSortedByDateAsc() {
        var pageRequest = PageRequest.of(0, 2)
                .withSort(Sort.by(Sort.Direction.ASC,"createdAt"));
        Flux<CourseEnrollment> enrollments = repository.findAllByCreatedBy(USERNAME_ONE, pageRequest);
        StepVerifier.create(enrollments)
                .expectNextMatches(enrollment ->
                        enrollment.getCreatedBy().equals(USERNAME_ONE) &&
                                enrollment.getCreatedAt().equals(ENROLLMENT_ONE_DATE))
                .expectNextMatches(enrollment ->
                        enrollment.getCreatedBy().equals(USERNAME_ONE) &&
                                enrollment.getCreatedAt().equals(ENROLLMENT_TWO_DATE))
                .verifyComplete();
    }

    @Test
    void findAllByCreatedBy_returnsEmptyListWhenUserHasNoEnrollments() {
        var pageRequest = PageRequest.of(0, 10)
                .withSort(Sort.by(Sort.Direction.ASC,"createdAt"));
        Flux<CourseEnrollment> enrollments = repository.findAllByCreatedBy("unknown user", pageRequest);
        StepVerifier.create(enrollments)
                .expectNextCount(0)
                .verifyComplete();
    }
}
