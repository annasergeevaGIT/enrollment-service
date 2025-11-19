package at.enrollment_service.service;

import at.enrollment_service.mapper.CourseClient;
import at.enrollment_service.dto.CreateEnrollmentRequest;
import at.enrollment_service.dto.EnrollmentResponse;
import at.enrollment_service.dto.GetCourseInfoRequest;
import at.enrollment_service.dto.SortBy;
import at.enrollment_service.mapper.EnrollmentMapper;
import at.enrollment_service.mapper.EnrollmentOutboxMapper;
import at.enrollment_service.repository.CourseEnrollmentRepository;
import at.enrollment_service.repository.EnrollmentPlacedEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseEnrollmentServiceImpl implements CourseEnrollmentService {

    private final CourseEnrollmentRepository repository;
    private final CourseClient courseClient;
    private final EnrollmentMapper enrollmentMapper;
    private final EnrollmentOutboxMapper enrollmentOutboxMapper;
    private final EnrollmentPlacedEventRepository enrollmentPlacedEventRepository;

    @Transactional
    public EnrollmentResponse createEnrollment(CreateEnrollmentRequest request, String username) {
        var getInfoRequest = new GetCourseInfoRequest(request.getCourseNames());
        var courseInfoResponse = courseClient.getCourseInfo(getInfoRequest);
        var enrollment = enrollmentMapper.mapToEnrollment(request, username, courseInfoResponse);
        var savedEnrollment = repository.save(enrollment);
        var outboxEvent = enrollmentOutboxMapper.toOrderOutbox(savedEnrollment);
        enrollmentPlacedEventRepository.save(outboxEvent);
        return enrollmentMapper.mapToResponse(savedEnrollment);
    }

    public List<EnrollmentResponse> getEnrollmentsOfUser(String username, SortBy sortBy, int from, int size) {
        var pageRequest = PageRequest.of(from, size, sortBy.getSort());
        return repository.findAllByCreatedBy(username, pageRequest)
                .stream()
                .map(enrollmentMapper::mapToResponse)
                .toList();
    }
}
