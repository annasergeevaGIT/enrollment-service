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
import org.springframework.web.client.RestClientException;

import at.enrollment_service.config.EnrollmentServiceProps;
import at.enrollment_service.dto.GetCourseInfoRequest;
import at.enrollment_service.dto.GetCourseInfoResponse;
import at.enrollment_service.exception.EnrollmentServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.SocketTimeoutException;
import java.util.concurrent.*;

@Component
@RequiredArgsConstructor
public class CourseClient {

    private final RestClient restClient;
    private final EnrollmentServiceProps props;

    private final ExecutorService executor =
            Executors.newVirtualThreadPerTaskExecutor();

    public GetCourseInfoResponse getCourseInfo(GetCourseInfoRequest request) {

        int attempts = props.getRetryCount() + 1;

        for (int attempt = 1; attempt <= attempts; attempt++) {

            try {
                // Run blocking RestClient on a virtual thread + enforce timeout
                CompletableFuture<GetCourseInfoResponse> future =
                        CompletableFuture.supplyAsync(() ->
                                        restClient.post()
                                                .uri(props.getCourseInfoPath())
                                                .body(request)
                                                .retrieve()
                                                .onStatus(HttpStatusCode::is5xxServerError,
                                                        (req, res) -> {
                                                            throw new EnrollmentServiceException(
                                                                    "Course Service Unavailable",
                                                                    HttpStatus.SERVICE_UNAVAILABLE
                                                            );
                                                        })
                                                .body(GetCourseInfoResponse.class),
                                executor);

                // Enforce timeout like WebClient.timeout(...)
                GetCourseInfoResponse response =
                        future.get(props.getDefaultTimeout().toMillis(), TimeUnit.MILLISECONDS);

                // ✔️ IMPORTANT: DO NOT treat partial success as failure
                return response;
            }
            catch (TimeoutException ex) {
                if (attempt == attempts) {
                    throw new EnrollmentServiceException(
                            "Failed to fetch course info after retries",
                            HttpStatus.SERVICE_UNAVAILABLE
                    );
                }
            }
            catch (EnrollmentServiceException ex) {
                if (attempt == attempts) throw ex;
            }
            catch (RestClientException ex) {
                if (attempt == attempts) {
                    throw new EnrollmentServiceException(
                            "Failed to fetch course info after retries",
                            HttpStatus.SERVICE_UNAVAILABLE
                    );
                }
            }
            catch (Exception ex) {
                if (attempt == attempts) {
                    throw new EnrollmentServiceException(
                            "Failed to fetch course info after retries",
                            HttpStatus.SERVICE_UNAVAILABLE
                    );
                }
            }

            // retry backoff
            sleep(props.getRetryBackoff().toMillis());
        }

        throw new IllegalStateException("Unreachable");
    }

    private void sleep(long millis) {
        try { Thread.sleep(millis); } catch (InterruptedException ignored) {}
    }
}


//@Component
//@RequiredArgsConstructor
//public class CourseClient {
//
//    private final RestClient restClient;
//    private final EnrollmentServiceProps props;
//
//    public GetCourseInfoResponse getCourseInfo(GetCourseInfoRequest request) {
//
//        int attempts = props.getRetryCount() + 1;
//
//        for (int i = 1; i <= attempts; i++) {
//
//            try {
//                return restClient.post()
//                        .uri(props.getCourseInfoPath())
//                        .body(request)
//                        .retrieve()
//                        .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
//                            throw new EnrollmentServiceException(
//                                    "Course Service Unavailable",
//                                    HttpStatus.SERVICE_UNAVAILABLE
//                            );
//                        })
//                        .body(GetCourseInfoResponse.class);
//            }
//            catch (EnrollmentServiceException | RestClientException ex) {
//
//                // retry unless it's the last attempt
//                if (i == attempts) {
//                    throw new EnrollmentServiceException(
//                            "Failed to fetch course info after retries",
//                            HttpStatus.SERVICE_UNAVAILABLE
//                    );
//                }
//            }
//
//            // wait before retry
//            sleep(props.getRetryBackoff().toMillis());
//        }
//
//        throw new IllegalStateException("Unreachable");
//    }
//
//    private void sleep(long millis) {
//        try { Thread.sleep(millis); }
//        catch (InterruptedException ignored) {}
//    }
//}
