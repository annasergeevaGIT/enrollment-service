package at.enrollment_service.service;

import at.enrollment_service.dto.CreateEnrollmentRequest;
import at.enrollment_service.dto.EnrollmentResponse;
import at.enrollment_service.dto.GetCourseInfoRequest;
import at.enrollment_service.dto.SortBy;
import at.enrollment_service.exception.EnrollmentServiceException;
import at.enrollment_service.mapper.CourseClient;
import at.enrollment_service.mapper.EnrollmentMapper;
import at.enrollment_service.mapper.EnrollmentOutboxMapper;
import at.enrollment_service.repository.CourseEnrollmentRepository;
import at.enrollment_service.repository.EnrollmentPlacedEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
    @Override
    public EnrollmentResponse createEnrollment(CreateEnrollmentRequest request, String username) {

        try {
            var courseInfo = courseClient.getCourseInfo(
                    new GetCourseInfoRequest(request.getCourseNames())
            );
            boolean hasUnavailable = courseInfo.getCourseInfos()
                    .stream()
                    .anyMatch(c -> Boolean.FALSE.equals(c.getIsAvailable()));

            if (hasUnavailable) {
                throw new EnrollmentServiceException(
                        "Some courses are not available",
                        HttpStatus.BAD_REQUEST
                );
            }

            var enrollment = enrollmentMapper.mapToEnrollment(request, username, courseInfo);
            var saved = repository.save(enrollment);

            var outbox = enrollmentOutboxMapper.toOrderOutbox(saved);
            enrollmentPlacedEventRepository.save(outbox);

            return enrollmentMapper.mapToResponse(saved);
        }
        catch (EnrollmentServiceException e) {

            throw e;
        }
        catch (Exception e) {
            log.error("Error creating enrollment", e);
            throw new EnrollmentServiceException(
                    "Internal error: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @Override
    public List<EnrollmentResponse> getEnrollmentsOfUser(String username, SortBy sortBy, int from, int size) {
        var pageRequest = org.springframework.data.domain.PageRequest.of(from, size)
                .withSort(sortBy.getSort());

        return repository.findAllByCreatedBy(username, pageRequest)
                .stream()
                .map(enrollmentMapper::mapToResponse)
                .toList();
    }
}



