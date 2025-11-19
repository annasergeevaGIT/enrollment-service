package at.enrollment_service.repository;

import at.enrollment_service.model.CourseEnrollment;
import at.enrollment_service.model.EnrollmentStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CourseEnrollmentRepository extends JpaRepository<CourseEnrollment, Long> {
    List<CourseEnrollment> findAllByCreatedBy(String createdBy, Pageable pageable);

    // CHANGE HERE: Add (clearAutomatically = true)
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE CourseEnrollment e SET e.status = :newStatus, e.updatedAt = CURRENT_TIMESTAMP WHERE e.id = :enrollmentId")
    void updateStatusById(@Param("enrollmentId") Long enrollmentId, @Param("newStatus") EnrollmentStatus newStatus);
}
