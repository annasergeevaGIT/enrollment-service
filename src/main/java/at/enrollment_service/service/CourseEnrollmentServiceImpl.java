package at.enrollment_service.service;

import at.enrollment_service.dto.CreateEnrollmentRequest;
import at.enrollment_service.dto.EnrollmentResponse;
import at.enrollment_service.dto.GetCourseInfoRequest;
import at.enrollment_service.dto.SortBy;
import at.enrollment_service.model.mapper.CourseClient;
import at.enrollment_service.model.mapper.EnrollmentMapper;
import at.enrollment_service.repository.CourseEnrollmentReopsitory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CourseEnrollmentServiceImpl implements CourseEnrollmentService {

    private final CourseEnrollmentReopsitory repository;
    private final CourseClient courseClient;
    private final EnrollmentMapper enrollmentMapper;

    @Override
    public Mono<EnrollmentResponse> createEnrollment(CreateEnrollmentRequest request, String username) {
        var getInfoRequest = new GetCourseInfoRequest(request.getCourseNames()); // wrap course names into dto object to pass to client
        return courseClient
                .getCourseInfo(getInfoRequest) // returns Mono<GetCourseInfoResponse> with course details.
                .map(response -> enrollmentMapper.mapToEnrollment(request, username, response)) // map to Enrollment entity
                .flatMap(repository::save) // save to repository and return saved entity as Mono
                .map(enrollmentMapper::mapToResponse); // map saved entity to EnrollmentResponse dto
    }

    @Override
    public Flux<EnrollmentResponse> getEnrollmentsOfUser(String username, SortBy sortBy, int from, int size) {
        var pageRequest = PageRequest.of(from, size)
                .withSort(sortBy.getSort());
        return repository.findAllByCreatedBy(username, pageRequest)
                .map(enrollmentMapper::mapToResponse);
    }
}
