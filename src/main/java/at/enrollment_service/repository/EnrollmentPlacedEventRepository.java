package at.enrollment_service.repository;

import at.enrollment_service.model.EnrollmentPlacedEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnrollmentPlacedEventRepository extends JpaRepository<EnrollmentPlacedEvent, Long> {
}