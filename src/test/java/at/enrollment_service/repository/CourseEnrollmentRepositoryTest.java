package at.enrollment_service.repository;

import at.enrollment_service.BaseTest;
import at.enrollment_service.model.CourseEnrollment;
import at.enrollment_service.model.EnrollmentStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static at.enrollment_service.testdata.TestConstants.*;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(

        scripts = {"/delete-enrollments.sql", "/insert-enrollments.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
class CourseEnrollmentRepositoryTest extends BaseTest {

    @Autowired
    private CourseEnrollmentReopsitory repository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findAllByCreatedBy_returnsCorrectSortedByDateDesc() {
        Pageable pageable = PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<CourseEnrollment> page = repository.findAllByCreatedBy(USERNAME_ONE, pageable);
        List<CourseEnrollment> enrollments = page.getContent();

        assertThat(enrollments).hasSize(2);


        assertThat(enrollments.get(0).getCreatedBy()).isEqualTo(USERNAME_ONE);
        assertThat(enrollments.get(0).getCreatedAt()).isEqualTo(ENROLLMENT_THREE_DATE);


        assertThat(enrollments.get(1).getCreatedBy()).isEqualTo(USERNAME_ONE);
        assertThat(enrollments.get(1).getCreatedAt()).isEqualTo(ENROLLMENT_TWO_DATE);
        assertThat(enrollments.get(1).getUpdatedAt()).isNotNull();
    }

    @Test
    void findAllByCreatedBy_returnsCorrectSortedByDateAsc() {
        Pageable pageable = PageRequest.of(0, 2, Sort.by(Sort.Direction.ASC, "createdAt"));
        Page<CourseEnrollment> page = repository.findAllByCreatedBy(USERNAME_ONE, pageable);
        List<CourseEnrollment> enrollments = page.getContent();

        assertThat(enrollments).hasSize(2);


        assertThat(enrollments.get(0).getCreatedAt()).isEqualTo(ENROLLMENT_ONE_DATE);


        assertThat(enrollments.get(1).getCreatedAt()).isEqualTo(ENROLLMENT_TWO_DATE);
    }

    @Test
    void findAllByCreatedBy_returnsEmptyListWhenUserHasNoEnrollments() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt"));
        Page<CourseEnrollment> page = repository.findAllByCreatedBy("unknown_user", pageable);
        assertThat(page.getContent()).isEmpty();
    }

//    @Test
//    void updateStatusById_updatesStatusOfExistingEnrollment() {
//        Long id = getIdByCreatedAt(ENROLLMENT_TWO_DATE, USERNAME_ONE);
//
//        CourseEnrollment original = repository.findById(id).orElseThrow();
//        LocalDateTime originalUpdatedAt = original.getUpdatedAt();
//
//        repository.updateStatusById(id, EnrollmentStatus.ACCEPTED);
//        entityManager.flush();
//        entityManager.clear();
//
//        CourseEnrollment updated = repository.findById(id).orElseThrow();
//        assertThat(updated.getStatus()).isEqualTo(EnrollmentStatus.ACCEPTED);
//        assertThat(updated.getUpdatedAt()).isNotNull();
//        assertThat(updated.getUpdatedAt()).isAfter(originalUpdatedAt);
//    }

    @Test
    void updateStatusById_doesNothingIfEnrollmentNotExists() {
        Long enrollmentId = 9999L;

        repository.updateStatusById(enrollmentId, EnrollmentStatus.ACCEPTED);

        Optional<CourseEnrollment> updated = repository.findById(enrollmentId);
        assertThat(updated).isEmpty();
    }

    protected Long getIdByCreatedAt(LocalDateTime createdAt, String username) {
        return entityManager.getEntityManager()
                .createQuery(
                        // FIX: Filter by both time and user
                        "select e.id from CourseEnrollment e where e.createdAt = :createdAt and e.createdBy = :createdBy",
                        Long.class)
                .setParameter("createdAt", createdAt)
                .setParameter("createdBy", username)
                .getSingleResult();
    }
}