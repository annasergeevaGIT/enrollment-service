package at.enrollment_service.service;

import at.enrollment_service.dto.CreateEnrollmentRequest;
import at.enrollment_service.dto.EnrollmentResponse;
import at.enrollment_service.dto.GetCourseInfoRequest;
import at.enrollment_service.dto.SortBy;
import at.enrollment_service.exception.EnrollmentServiceException;
import at.enrollment_service.model.mapper.CourseClient;
import at.enrollment_service.model.mapper.EnrollmentMapper;
import at.enrollment_service.model.mapper.EnrollmentOutboxMapper;
import at.enrollment_service.repository.CourseEnrollmentReopsitory;
import at.enrollment_service.repository.EnrollmentPlacedEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseEnrollmentServiceImpl implements CourseEnrollmentService {

    private final CourseEnrollmentReopsitory repository;
    private final CourseClient courseClient;
    private final EnrollmentMapper enrollmentMapper;

    private final EnrollmentOutboxMapper enrollmentOutboxMapper; // Outbox table save
    private final EnrollmentPlacedEventRepository enrollmentPlacedEventRepository; // Outbox table save

    @Transactional
    @Override
    public Mono<EnrollmentResponse> createEnrollment(CreateEnrollmentRequest request, String username) {
        var getInfoRequest = new GetCourseInfoRequest(request.getCourseNames()); // wrap course names into dto object to pass to client
        return courseClient
                .getCourseInfo(getInfoRequest) // returns Mono<GetCourseInfoResponse> with course details.
                .map(response -> enrollmentMapper.mapToEnrollment(request, username, response)) // map to Enrollment entity
                .flatMap(repository::save) // save to repository and return saved entity as Mono
                .zipWhen(menuOrder -> {
                    var outbox = enrollmentOutboxMapper.toOrderOutbox(menuOrder);
                    return enrollmentPlacedEventRepository.save(outbox);
                })
                .map(tuple -> enrollmentMapper.mapToResponse(tuple.getT1()))
                .doOnError(e -> log.error("Error saving MenuOrder: {}", e.getMessage()))
                .onErrorMap(this::handleThrowable);
    }

    @Override
    public Flux<EnrollmentResponse> getEnrollmentsOfUser(String username, SortBy sortBy, int from, int size) {
        var pageRequest = PageRequest.of(from, size)
                .withSort(sortBy.getSort());
        return repository.findAllByCreatedBy(username, pageRequest)
                .map(enrollmentMapper::mapToResponse);
    }

    private Throwable handleThrowable(Throwable t) {
        return (t instanceof EnrollmentServiceException) ? t :
                new EnrollmentServiceException(t.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
