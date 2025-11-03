package at.enrollment_service.repository;

import at.enrollment_service.model.CourseEnrollment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;


public interface CourseEnrollmentReopsitory extends ReactiveCrudRepository<CourseEnrollment, Long> {

    //Pageable limits query results using page, size, and sort parameters, instead of loading all data at once
    Flux<CourseEnrollment> findAllByCreatedBy(String username, Pageable pageable);
}
