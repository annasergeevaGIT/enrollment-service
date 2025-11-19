package at.enrollment_service;

import at.enrollment_service.testdata.TestConstants;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static at.enrollment_service.testdata.TestConstants.DELAY_MILLIS;
import static at.enrollment_service.testdata.TestConstants.COURSE_INFO_PATH;
import static at.enrollment_service.testdata.TestDataProvider.readPartiallySuccessfulResponse;
import static at.enrollment_service.testdata.TestDataProvider.readSuccessfulResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;


@SpringBootTest
public class BaseIntegrationTest extends BaseTest {
    @Autowired
    private EntityManager em;

    @RegisterExtension // registers a JUnit 5 extension that manages the lifecycle of the WireMock server
    protected static WireMockExtension wiremock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @DynamicPropertySource //dynamically set application properties at test runtime.
    static void applyProperties(DynamicPropertyRegistry registry) {
        registry.add("external.course-service-url", wiremock::baseUrl); //overrides external.course-service-url from application.yml with the running WireMock server’s URL.
    }

    protected void prepareStubForServiceUnavailable() {
        wiremock.stubFor(post(COURSE_INFO_PATH)
                .willReturn(serviceUnavailable()));
    }

    protected void prepareStubForSuccessWithTimeout() {
        var responseBody = readSuccessfulResponse();
        long requiredDelay = TestConstants.DEFAULT_TIMEOUT.toMillis() * 2 + 200;
        wiremock.stubFor(post(COURSE_INFO_PATH)
                .willReturn(okJson(responseBody).withFixedDelay((int)requiredDelay))
        );
    }

    protected void prepareStubForPartialSuccess() {
        var responseBody = readPartiallySuccessfulResponse();
        wiremock.stubFor(post(COURSE_INFO_PATH)
                .willReturn(okJson(responseBody))
        );
    }

    protected void prepareStubForSuccess() {
        var responseBody = readSuccessfulResponse();
        wiremock.stubFor(post(COURSE_INFO_PATH)
                .willReturn(okJson(responseBody)));
    }

    protected Long getEnrollmentIdByCourseId(Long id) {
        return em.createQuery("select r.id from CourseEnrollment r where r.id= ?1", Long.class)
                .setParameter(1, id)
                .getSingleResult();
    }
}
