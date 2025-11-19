package at.enrollment_service.mapper;

import at.enrollment_service.config.EnrollmentServiceProps;
import at.enrollment_service.dto.GetCourseInfoRequest;
import at.enrollment_service.dto.GetCourseInfoResponse;
import at.enrollment_service.exception.EnrollmentServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.ResourceAccessException;

@Component
@RequiredArgsConstructor
public class CourseClient {

    private final RestClient.Builder restClientBuilder;
    private final EnrollmentServiceProps props;

    @Retryable(
            retryFor = {ResourceAccessException.class, EnrollmentServiceException.class, RuntimeException.class},
            maxAttemptsExpression = "#{@enrollmentServiceProps.retryCount}",
            backoff = @Backoff(delayExpression = "#{@enrollmentServiceProps.retryBackoff.toMillis()}")
    )
    public GetCourseInfoResponse getCourseInfo(GetCourseInfoRequest request) {
        // FIX: Construct full absolute URL to avoid "Target host is not specified"
        String fullUrl = props.getCourseServiceUrl() + props.getCourseInfoPath();

        return restClientBuilder.build()
                .post()
                .uri(fullUrl) // <--- Uses absolute URL (http://localhost:...)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .onStatus(HttpStatusCode::is5xxServerError, (req, resp) -> {
                    throw new EnrollmentServiceException(
                            "Course Service Unavailable",
                            HttpStatus.valueOf(resp.getStatusCode().value())
                    );
                })
                .body(GetCourseInfoResponse.class);
    }
}