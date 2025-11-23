package at.enrollment_service;

import at.enrollment_service.config.RestClientConfig;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static at.enrollment_service.testdata.TestConstants.COURSE_INFO_PATH;
import static at.enrollment_service.testdata.TestConstants.DELAY_MILLIS;
import static at.enrollment_service.testdata.TestDataProvider.*;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

@SpringBootTest
@Import(TestRestClientConfig.class)
@ImportAutoConfiguration(exclude = RestClientConfig.class)
public abstract class BaseIntegrationTest extends BaseTest {

    @RegisterExtension
    protected static final WireMockExtension wiremock =
            WireMockExtension.newInstance()
                    .options(wireMockConfig().dynamicPort())
                    .build();

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {

        registry.add("external.course-service-url", wiremock::baseUrl);

        registry.add("external.course-info-path", () -> COURSE_INFO_PATH);
    }

    protected void prepareStubForServiceUnavailable() {
        wiremock.stubFor(
                post(urlEqualTo(COURSE_INFO_PATH))
                        .willReturn(serviceUnavailable())
        );
    }

    protected void prepareStubForSuccessWithTimeout() {
        wiremock.stubFor(
                post(urlEqualTo(COURSE_INFO_PATH))
                        .willReturn(
                                okJson(readSuccessfulResponse())
                                        .withBody("{}".repeat(10))      // ensure body exists
                                        .withChunkedDribbleDelay(1, DELAY_MILLIS) // delay header + body
                        )
        );
    }

    protected void prepareStubForPartialSuccess() {
        wiremock.stubFor(
                post(urlEqualTo(COURSE_INFO_PATH))
                        .willReturn(okJson(readPartiallySuccessfulResponse()))
        );
    }

    protected void prepareStubForSuccess() {
        wiremock.stubFor(
                post(urlEqualTo(COURSE_INFO_PATH))
                        .willReturn(okJson(readSuccessfulResponse()))
        );
    }
}
