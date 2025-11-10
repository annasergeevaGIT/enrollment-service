package at.enrollment_service.model;

import at.enrollment_service.exception.EnrollmentServiceException;
import org.springframework.http.HttpStatus;

public enum EnrollmentStatus {
    NEW,
    ACCEPTED,
    REJECTED;

    public static EnrollmentStatus fromString(String str) {
        try {
            return EnrollmentStatus.valueOf(str.toUpperCase());
        } catch (IllegalArgumentException e) {
            var msg = "Failed to create EnrollmentStatus from string: %s".formatted(str);
            throw new EnrollmentServiceException(msg, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
