package at.enrollment_service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class EnrollmentServiceException extends RuntimeException{
    private final HttpStatus status;

    public EnrollmentServiceException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
