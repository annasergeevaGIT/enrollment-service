package at.enrollment_service.model.mapper;

import at.enrollment_service.config.EnrollmentServiceProps;
import at.enrollment_service.dto.GetCourseInfoRequest;
import at.enrollment_service.dto.GetCourseInfoResponse;
import at.enrollment_service.exception.EnrollmentServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeoutException;

@Component
@RequiredArgsConstructor
public class CourseClient {

    private final RestClient restClient;
    private final EnrollmentServiceProps props;

    public GetCourseInfoResponse getCourseInfo(GetCourseInfoRequest request) {
        int attempts = 0;
        Duration backoff = props.getRetryBackoff();

        while (true) {
            attempts++;
            try {
                return doRequest(request);
            } catch (EnrollmentServiceException | TimeoutException ex) {
                if (attempts >= props.getRetryCount()) {
                    String msg = "Failed to fetch course info from Course Service after max retry attempts";
                    throw new EnrollmentServiceException(msg, HttpStatus.SERVICE_UNAVAILABLE);
                }

                sleepWithJitter(backoff, props.getRetryJitter());
                backoff = backoff.multipliedBy(2);
            }
        }
    }

    private GetCourseInfoResponse doRequest(GetCourseInfoRequest request) throws TimeoutException {
        try {
            return restClient
                    .post()
                    .uri(props.getCourseInfoPath())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::is5xxServerError, (req, resp) -> {
                        throw new EnrollmentServiceException("Course Service Unavailable", HttpStatus.SERVICE_UNAVAILABLE);
                    })
                    .body(GetCourseInfoResponse.class);
        } catch (ResourceAccessException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof java.net.SocketTimeoutException
                    || cause instanceof java.net.http.HttpTimeoutException) {
                throw new TimeoutException("Course Service call timed out");
            }
            throw ex;
        }
    }

    private void sleepWithJitter(Duration base, double jitterFactor) {
        long baseMillis = base.toMillis();
        double min = 1.0 - jitterFactor;
        double max = 1.0 + jitterFactor;
        double jitter = ThreadLocalRandom.current().nextDouble(min, max);
        long sleepMillis = (long) (baseMillis * jitter);

        try {
            Thread.sleep(sleepMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}