package at.enrollment_service.repository;

import at.enrollment_service.model.EnrollmentPlacedEvent;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;


public interface EnrollmentPlacedEventRepository extends ReactiveCrudRepository<EnrollmentPlacedEvent, Long> {
}