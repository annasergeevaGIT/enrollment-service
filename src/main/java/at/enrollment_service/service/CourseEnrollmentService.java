package at.enrollment_service.service;

import at.enrollment_service.dto.CreateEnrollmentRequest;
import at.enrollment_service.dto.EnrollmentResponse;
import at.enrollment_service.dto.SortBy;

import java.util.List;

public interface CourseEnrollmentService {
    EnrollmentResponse createEnrollment(CreateEnrollmentRequest request, String username);
    List<EnrollmentResponse> getEnrollmentsOfUser(String username, SortBy sortBy, int from, int size);
}
