package at.enrollment_service.repository;

import at.enrollment_service.model.CourseEnrollment;
import at.enrollment_service.model.EnrollmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface CourseEnrollmentReopsitory extends JpaRepository<CourseEnrollment, Long> {

    //Pageable limits query results using page, size, and sort parameters, instead of loading all data at once
    Page<CourseEnrollment> findAllByCreatedBy(String username, Pageable pageable);

    @Transactional
    @Modifying
    @Query("UPDATE CourseEnrollment e SET e.status = :status WHERE e.id = :id")
    void updateStatusById(@Param("id") Long id, @Param("status") EnrollmentStatus newStatus);
}
