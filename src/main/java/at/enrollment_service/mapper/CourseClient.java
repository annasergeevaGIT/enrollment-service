package at.enrollment_service.mapper;

import at.enrollment_service.config.EnrollmentServiceProps;
import at.enrollment_service.dto.GetCourseInfoRequest;
import at.enrollment_service.dto.GetCourseInfoResponse;
import at.enrollment_service.exception.EnrollmentServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class CourseClient {

    private final RestClient restClient;
    private final EnrollmentServiceProps props;

    public GetCourseInfoResponse getCourseInfo(GetCourseInfoRequest request) {

        int attempts = Math.max(1, props.getRetryCount() + 1);

        for (int attempt = 1; attempt <= attempts; attempt++) {
            try {
                return restClient.post()
                        .uri(props.getCourseInfoPath()) // /v1/courses/course-info
                        .body(request)
                        .retrieve()
                        .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                            throw new EnrollmentServiceException(
                                    "Course Service Unavailable",
                                    HttpStatus.SERVICE_UNAVAILABLE
                            );
                        })
                        .body(GetCourseInfoResponse.class);

            } catch (EnrollmentServiceException e) {
                if (attempt == attempts) throw e;
            } catch (RuntimeException e) {
                if (attempt == attempts)
                    throw new EnrollmentServiceException("Failed to fetch course info: " + e.getMessage(),
                            HttpStatus.SERVICE_UNAVAILABLE);
            }

            try {
                Thread.sleep(props.getRetryBackoff().toMillis());
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
                throw new EnrollmentServiceException(
                        "Interrupted while retrying course info",
                        HttpStatus.INTERNAL_SERVER_ERROR
                );
            }
        }

        throw new EnrollmentServiceException(
                "Failed to fetch course info after retries",
                HttpStatus.SERVICE_UNAVAILABLE
        );
    }
}
