package at.enrollment_service.repository;

import at.enrollment_service.config.JpaConfig;
import at.enrollment_service.model.CourseEnrollment;
import at.enrollment_service.model.EnrollmentStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaConfig.class)
public class CourseEnrollmentRepositoryTest {

    @Autowired
    private CourseEnrollmentRepository repository;

    @Test
    void updateStatusById_updatesStatus() {
        CourseEnrollment enrollment = createDummyEnrollment("user1");
        enrollment = repository.save(enrollment);
        repository.updateStatusById(enrollment.getId(), EnrollmentStatus.ACCEPTED);
        var updated = repository.findById(enrollment.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(EnrollmentStatus.ACCEPTED);
    }

    @Test
    void findAllByCreatedBy_returnsCorrectList_SortedByDateDesc() {

        var enroll1 = repository.save(createDummyEnrollment("targetUser"));
        var enroll2 = repository.save(createDummyEnrollment("targetUser"));
        var enrollOther = repository.save(createDummyEnrollment("otherUser"));
        var pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<CourseEnrollment> result = repository.findAllByCreatedBy("targetUser", pageable);
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(enroll2.getId());
        assertThat(result.get(1).getId()).isEqualTo(enroll1.getId());
    }

    @Test
    void findAllByCreatedBy_returnsCorrectList_SortedByDateAsc() {

        var enroll1 = repository.save(createDummyEnrollment("targetUser"));
        var enroll2 = repository.save(createDummyEnrollment("targetUser"));
        var pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "createdAt"));
        List<CourseEnrollment> result = repository.findAllByCreatedBy("targetUser", pageable);
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(enroll1.getId());
        assertThat(result.get(1).getId()).isEqualTo(enroll2.getId());
    }

    @Test
    void findAllByCreatedBy_returnsEmpty_WhenUserDoesNotExist() {
        repository.save(createDummyEnrollment("someUser"));

        var pageable = PageRequest.of(0, 10);
        List<CourseEnrollment> result = repository.findAllByCreatedBy("unknownUser", pageable);

        assertThat(result).isEmpty();
    }

    private CourseEnrollment createDummyEnrollment(String username) {
        return CourseEnrollment.builder()
                .totalPrice(BigDecimal.TEN)
                .city("Vienna")
                .street("TestStreet")
                .house(1)
                .apartment(1)
                .status(EnrollmentStatus.NEW)
                .createdBy(username)
                .courseLineItems(Collections.emptyList())
                .build();
    }
}