package at.enrollment_service.repository;

import at.enrollment_service.model.CourseEnrollment;
import at.enrollment_service.model.EnrollmentStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public interface CourseEnrollmentRepository extends ReactiveCrudRepository<CourseEnrollment, Long> {

    //Pageable limits query results using page, size, and sort parameters, instead of loading all data at once
    Flux<CourseEnrollment> findAllByCreatedBy(String username, Pageable pageable);

    @Transactional
    @Modifying
    @Query("UPDATE enrollments SET status = :newStatus, updated_at = CURRENT_TIMESTAMP WHERE id = :enrollmentId")
    Mono<Void> updateStatusById(Long enrollmentId, EnrollmentStatus newStatus);
}
