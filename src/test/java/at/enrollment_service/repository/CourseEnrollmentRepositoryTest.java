package at.enrollment_service.repository;

import at.enrollment_service.BaseTest;
import at.enrollment_service.model.CourseEnrollment;
import at.enrollment_service.model.EnrollmentStatus;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static at.enrollment_service.testdata.TestConstants.*;
import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
@Transactional(propagation = Propagation.NEVER)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class CourseEnrollmentRepositoryTest extends BaseTest {

    @Autowired
    private CourseEnrollmentRepository repository;

    @Test
    void findAllByCreatedBy_SortedDesc() {
        var result = repository.findAllByCreatedBy(USERNAME_ONE,
                PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "createdAt")));

        assertThat(result.getContent().get(0).getCreatedAt())
                .isEqualTo(ENROLLMENT_THREE_DATE);

        assertThat(result.getContent().get(1).getCreatedAt())
                .isEqualTo(ENROLLMENT_TWO_DATE);
    }

    @Test
    void findAllByCreatedBy_returnsCorrectSortedByDateDesc() {
        var pageRequest = PageRequest.of(0, 2)
                .withSort(Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<CourseEnrollment> result =
                repository.findAllByCreatedBy(USERNAME_ONE, pageRequest);

        assertThat(result.getContent()).hasSize(2);

        assertThat(result.getContent().get(0).getCreatedBy()).isEqualTo(USERNAME_ONE);
        assertThat(result.getContent().get(0).getCreatedAt()).isEqualTo(ENROLLMENT_THREE_DATE);

        assertThat(result.getContent().get(1).getCreatedBy()).isEqualTo(USERNAME_ONE);
        assertThat(result.getContent().get(1).getCreatedAt()).isEqualTo(ENROLLMENT_TWO_DATE);
    }

    @Test
    void findAllByCreatedBy_returnsCorrectSortedByDateAsc() {
        var pageRequest = PageRequest.of(0, 2)
                .withSort(Sort.by(Sort.Direction.ASC,"createdAt"));

        // 2. Switched from Flux to Page<CourseEnrollment>
        Page<CourseEnrollment> result = repository.findAllByCreatedBy(USERNAME_ONE, pageRequest);

        // 3. Switched from StepVerifier assertions to AssertJ on the list content
        assertThat(result.getContent()).hasSize(2);

        // Check first enrollment (oldest)
        assertThat(result.getContent().get(0).getCreatedBy()).isEqualTo(USERNAME_ONE);
        assertThat(result.getContent().get(0).getCreatedAt()).isEqualTo(ENROLLMENT_ONE_DATE);

        // Check second enrollment
        assertThat(result.getContent().get(1).getCreatedBy()).isEqualTo(USERNAME_ONE);
        assertThat(result.getContent().get(1).getCreatedAt()).isEqualTo(ENROLLMENT_TWO_DATE);
    }

    @Test
    void findAllByCreatedBy_returnsEmptyListWhenUserHasNoEnrollments() {
        var pageRequest = PageRequest.of(0, 10)
                .withSort(Sort.by(Sort.Direction.ASC,"createdAt"));

        // 2. Switched from Flux to Page<CourseEnrollment>
        Page<CourseEnrollment> result = repository.findAllByCreatedBy("unknown user", pageRequest);

        // 3. Switched from StepVerifier to AssertJ
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void updateStatusById_updatesStatusOfExistingEnrollment() {
        var enrollment = repository.findAll().getFirst();
        var enrollmentId = enrollment.getId();

        repository.updateStatusById(enrollmentId, EnrollmentStatus.ACCEPTED);

        var updated = repository.findById(enrollmentId).orElse(null);

        assertThat(updated).isNotNull();
        assertThat(updated.getStatus()).isEqualTo(EnrollmentStatus.ACCEPTED);

        // NEW: only check updatedAt is now set
        assertThat(updated.getUpdatedAt()).isNotNull();
    }

    @Test
    void updateStatusById_doesNothingIfEnrollmentNotExists() {
        Long enrollmentId = 1000L;
        repository.updateStatusById(enrollmentId, EnrollmentStatus.ACCEPTED);

        // 6. Switched from .block() to synchronous Optional.orElse() call
        var updated = repository.findById(enrollmentId).orElse(null);

        AssertionsForClassTypes.assertThat(updated).isNull();
    }
}
