package at.enrollment_service.repository;

import at.enrollment_service.BaseTest;
import at.enrollment_service.config.R2dbcConfig;
import at.enrollment_service.model.CourseEnrollment;
import at.enrollment_service.model.EnrollmentStatus;
import org.assertj.core.api.AssertionsForClassTypes;
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
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@Import({R2dbcConfig.class}) // import custom converters configuration
@ImportAutoConfiguration({JacksonAutoConfiguration.class}) //ensure Jackson config is loaded
@DataR2dbcTest //slice Test loads R2DBC components, rolls back after each test
public class CourseEnrollmentRepositoryTest extends BaseTest {

    @Autowired
    private CourseEnrollmentRepository repository;

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

    @Test
    void updateStatusById_updatesStatusOfExistingEnrollment() {
        var enrollment = repository.findAll().blockFirst();
        var enrollmentId = enrollment.getId();

        repository.updateStatusById(enrollmentId, EnrollmentStatus.ACCEPTED).block();
        var updated = repository.findById(enrollmentId).block();
        assertThat(updated.getStatus()).isEqualTo(EnrollmentStatus.ACCEPTED);
        AssertionsForClassTypes.assertThat(updated.getUpdatedAt()).isAfter(enrollment.getUpdatedAt());
    }

    @Test
    void updateStatusById_doesNothingIfEnrollmentNotExists() {
        Long enrollmentId = 1000L;

        repository.updateStatusById(enrollmentId, EnrollmentStatus.ACCEPTED).block();
        var updated = repository.findById(enrollmentId).block();
        AssertionsForClassTypes.assertThat(updated).isNull();
    }
}
