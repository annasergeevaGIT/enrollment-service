package at.enrollment_service.model.mapper;

import at.enrollment_service.model.CourseEnrollment;
import at.enrollment_service.model.EnrollmentPlacedEvent;
import org.springframework.stereotype.Component;

@Component
public class EnrollmentOutboxMapper {

    public EnrollmentPlacedEvent toOrderOutbox(CourseEnrollment enrollment) {
        return EnrollmentPlacedEvent.builder()
                .enrollmentId(enrollment.getId())
                .createdBy(enrollment.getCreatedBy())
                .city(enrollment.getCity())
                .street(enrollment.getStreet())
                .house(enrollment.getHouse())
                .apartment(enrollment.getApartment())
                .createdAt(enrollment.getCreatedAt())
                .build();
    }
}