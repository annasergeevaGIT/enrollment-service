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

import java.util.List;

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
    public EnrollmentResponse createEnrollment(CreateEnrollmentRequest request, String username) {
        try {
            var courseInfoRequest = new GetCourseInfoRequest(request.getCourseNames());
            var courseInfoResponse = courseClient.getCourseInfo(courseInfoRequest);
            var enrollment = enrollmentMapper.mapToEnrollment(request, username, courseInfoResponse);
            var savedEnrollment = repository.save(enrollment);
            var outboxEvent = enrollmentOutboxMapper.toOrderOutbox(savedEnrollment);
            enrollmentPlacedEventRepository.save(outboxEvent);
            return enrollmentMapper.mapToResponse(savedEnrollment);

        } catch (Exception e) {
            log.error("Error saving Enrollment: {}", e.getMessage());
            throw handleThrowable(e);
        }
    }

    @Override
    public List<EnrollmentResponse> getEnrollmentsOfUser(String username, SortBy sortBy, int from, int size) {
        var pageRequest = PageRequest.of(from, size, sortBy.getSort());

        return repository.findAllByCreatedBy(username, pageRequest)
                .getContent()
                .stream()
                .map(enrollmentMapper::mapToResponse)
                .toList();
    }

    private EnrollmentServiceException handleThrowable(Throwable t) {
        if (t instanceof EnrollmentServiceException e) return e;
        return new EnrollmentServiceException(t.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
