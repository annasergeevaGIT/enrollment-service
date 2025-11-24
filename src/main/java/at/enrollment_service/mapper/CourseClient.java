package at.enrollment_service.mapper;

import at.enrollment_service.config.EnrollmentServiceProps;
import at.enrollment_service.dto.GetCourseInfoRequest;
import at.enrollment_service.dto.GetCourseInfoResponse;
import at.enrollment_service.exception.EnrollmentServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerExchangeFilterFunction;
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
        return restClient.post()
                .uri(props.getCourseInfoPath())   // /v1/courses/course-info
                .body(request)
                .retrieve()
                .body(GetCourseInfoResponse.class);
    }
}