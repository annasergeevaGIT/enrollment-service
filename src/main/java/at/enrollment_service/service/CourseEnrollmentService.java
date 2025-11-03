package at.enrollment_service.service;

import at.enrollment_service.dto.CreateEnrollmentRequest;
import at.enrollment_service.dto.EnrollmentResponse;
import at.enrollment_service.dto.SortBy;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CourseEnrollmentService {
    Mono<EnrollmentResponse> createEnrollment(CreateEnrollmentRequest request, String username);
    Flux<EnrollmentResponse> getEnrollmentsOfUser(String username, SortBy sortBy, int from, int size);
}
