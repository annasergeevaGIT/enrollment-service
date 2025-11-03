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
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.util.concurrent.TimeoutException;

/*
 * Web Client for interacting with the Course Service.
 */
@Component
@RequiredArgsConstructor
public class CourseClient {

    private final WebClient webClient;
    private final EnrollmentServiceProps props;

    public Mono<GetCourseInfoResponse> getCourseInfo(GetCourseInfoRequest request) {
        return webClient
                .post()
                .uri(props.getCourseInfoPath())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::is5xxServerError, response -> //5xx error from Course Service
                        Mono.error(new EnrollmentServiceException("Course Service Unavailable", HttpStatus.SERVICE_UNAVAILABLE)))
                .bodyToMono(GetCourseInfoResponse.class)
                .timeout(props.getDefaultTimeout())                     //timeout for Course Service response
                .retryWhen(                                             //timeout applies to each retry attempt. (If retry is placed before, then timeout applied only to first attempt)
                        Retry.backoff(props.getRetryCount(), props.getRetryBackoff())  //exponential growth for retries intervals
                                .jitter(props.getRetryJitter())          //adds randomness to avoid CourseService instances sending retries simultaneously
                                .filter(t -> {                           //repeat retry only on EnrollmentServiceException (5xx from Course Service) or TimeoutException
                                    return t instanceof EnrollmentServiceException || t instanceof TimeoutException;
                                })
                                .onRetryExhaustedThrow(((retryBackoffSpec, retrySignal) -> {  //after max retries, throw EnrollmentServiceException with SERVICE_UNAVAILABLE
                                    var msg = "Failed to fetch course info from Course Service after max retry attempts";
                                    throw new EnrollmentServiceException(msg, HttpStatus.SERVICE_UNAVAILABLE);
                                }))
                );
    }
}
