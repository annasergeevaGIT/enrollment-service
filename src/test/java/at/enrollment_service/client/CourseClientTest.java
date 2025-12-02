package at.enrollment_service.client;

import at.enrollment_service.config.EnrollmentServiceProps;
import at.enrollment_service.dto.CourseInfo;
import at.enrollment_service.dto.GetCourseInfoRequest;
import at.enrollment_service.dto.GetCourseInfoResponse;
import at.enrollment_service.exception.EnrollmentServiceException;
import at.enrollment_service.mapper.CourseClient;
import at.enrollment_service.testdata.TestDataProvider;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.net.http.HttpClient;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static at.enrollment_service.testdata.TestConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CourseClientTest {

    private final EnrollmentServiceProps props = new EnrollmentServiceProps(
            "http://localhost:8081",
            "/api/courses/enrollment-info",
            DEFAULT_TIMEOUT,
            RETRY_BACKOFF,
            RETRY_COUNT,
            RETRY_JITTER
    );

    private CourseClient courseClient;
    private MockWebServer mockWebServer;


}
