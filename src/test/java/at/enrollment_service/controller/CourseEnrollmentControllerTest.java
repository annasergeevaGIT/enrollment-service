package at.enrollment_service.controller;

import at.enrollment_service.BaseIntegrationTest;
import at.enrollment_service.dto.EnrollmentResponse;
import at.enrollment_service.model.EnrollmentStatus;

import at.enrollment_service.testdata.AuthToken;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.assertj.core.api.AssertionsForInterfaceTypes;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Comparator;

import static at.enrollment_service.testdata.TestConstants.*;
import static at.enrollment_service.testdata.TestDataProvider.*;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.springframework.web.reactive.function.BodyInserters.fromFormData;

@AutoConfigureWebTestClient(timeout = "20000") // Increase timeout for WebTestClient
@EnableAutoConfiguration(exclude = {KafkaAutoConfiguration.class})
public class CourseEnrollmentControllerTest extends BaseIntegrationTest {

    private static final KeycloakContainer KEYCLOAK = new KeycloakContainer("quay.io/keycloak/keycloak:24.0")
            .withRealmImportFile("/cloud-java-realm.json");

    static {
        KEYCLOAK.start();
    }

    private static AuthToken admin;
    private static AuthToken userWithEnrollments;
    private static AuthToken userNoEnrollments;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", () -> KEYCLOAK.getAuthServerUrl() + "/realms/cloud-java");
        registry.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri", () -> KEYCLOAK.getAuthServerUrl() + "/realms/cloud-java/protocol/openid-connect/certs");
    }

    @BeforeAll
    static void setup() {
        WebClient webClient = WebClient.builder()
                .baseUrl(KEYCLOAK.getAuthServerUrl() + "/realms/cloud-java/protocol/openid-connect/token")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .build();
        admin = createToken(webClient, ADMIN_USERNAME, "password");
        userWithEnrollments = createToken(webClient, USERNAME_ONE, "password");
        userNoEnrollments = createToken(webClient, USERNAME_NO_ENROLLMENTS, "password");
    }

    @Autowired
    protected WebTestClient webTestClient;

    @Test
    void submitCourseEnrollment_returnsCorrectResponse() {
        prepareStubForSuccess();
        var validRequest = createEnrollmentRequest();
        var expectedCourseItems = createdItems();
        webTestClient.post()
                .uri(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(h -> h.setBearerAuth(userWithEnrollments.getAccessToken()))
                .bodyValue(validRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(EnrollmentResponse.class)
                .value(response -> {
                    assertThat(response.getEnrollmentId()).isNotNull();
                    AssertionsForInterfaceTypes.assertThat(response.getCourseLineItems()).isEqualTo(expectedCourseItems);
                    AssertionsForInterfaceTypes.assertThat(response.getStatus()).isEqualTo(EnrollmentStatus.NEW);
                    assertThat(response.getTotalPrice()).isEqualTo(SUCCESS_TOTAL_PRICE);
                    assertThat(response.getAddress()).isEqualTo(validRequest.getAddress());
                });
    }

    @Test
    void submitCourseEnrollment_returnsNotFound_whenSomeCoursesAreNotAvailableInCourseService() {
        prepareStubForPartialSuccess();
        var request = createEnrollmentRequest();
        webTestClient.post()
                .uri(BASE_URL)
                .headers(h -> h.setBearerAuth(userWithEnrollments.getAccessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getEnrollmentsOfUser_returnsUnauthorized_whenUserIsNotAuthenticated() {
        webTestClient.get()
                .uri(BASE_URL + "?from=0&size=10&sortBy=date_asc")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void getEnrollmentsOfUser_returnsForbidden_whenUserHasNoRights() {
        webTestClient.get()
                .uri(BASE_URL + "?from=0&size=10&sortBy=date_asc")
                .headers(h -> h.setBearerAuth(admin.getAccessToken()))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void getEnrollmentsOfUser_returnsEmptyListOfEnrollments_whenUserHasNoEnrollments() {
        webTestClient.get()
                .uri(BASE_URL + "?from=0&size=10&sortBy=date_asc")
                .headers(h -> h.setBearerAuth(userNoEnrollments.getAccessToken()))
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(EnrollmentResponse.class)
                .value(enrollments -> {
                    assertThat(enrollments).isEmpty();
                });
    }

    @Test
    void getEnrollmentsOfUser_returnsCorrectlySortedListOfEnrollments() {
        webTestClient.get()
                .uri(BASE_URL + "?from=0&size=10&sortBy=date_asc")
                .headers(h -> h.setBearerAuth(userWithEnrollments.getAccessToken()))
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(EnrollmentResponse.class)
                .value(enrollments -> {
                    AssertionsForInterfaceTypes.assertThat(enrollments).hasSize(3)
                            .isSortedAccordingTo(Comparator.comparing(EnrollmentResponse::getCreatedAt));
                });
    }

    @Test
    void submitCourseEnrollment_returnsBadRequest_whenEnrollmentInvalid() {
        var invalidRequest = createEnrollmentInvalidRequest();
        webTestClient.post()
                .uri(BASE_URL)
                .headers(h -> h.setBearerAuth(userWithEnrollments.getAccessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidRequest)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void submitCourseEnrollment_returnsUnauthorized_whenUserIsNotAuthenticated() {
        var validRequest = createEnrollmentRequest();
        webTestClient.post()
                .uri(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validRequest)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void submitCourseEnrollment_returnsForbidden_whenUserHasNoRights() {
        var validRequest = createEnrollmentRequest();
        webTestClient.post()
                .uri(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(h -> h.setBearerAuth(admin.getAccessToken()))
                .bodyValue(validRequest)
                .exchange()
                .expectStatus().isForbidden();
    }


    @Test
    void submitCourseEnrollment_returnsServiceUnavailableOnTimeout() {
        prepareStubForSuccessWithTimeout();
        var validRequest = createEnrollmentRequest();
        webTestClient.post()
                .uri(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(h -> h.setBearerAuth(userWithEnrollments.getAccessToken()))
                .bodyValue(validRequest)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void getEnrollmentsOfUser_returnsBadRequestForInvalidParams() {
        webTestClient.get()
                .uri(BASE_URL + "?from=-1&size=10")
                .headers(h -> h.setBearerAuth(userWithEnrollments.getAccessToken()))
                .exchange()
                .expectStatus().isBadRequest();
    }

    private static AuthToken createToken(WebClient webClient, String username, String password) {
        return webClient.post()
                .body(fromFormData("grant_type", "password")
                        .with("client_id", "cloud-java-gateway")
                        .with("username", username)
                        .with("password", password)
                        .with("client_secret", "RleFn4MVPDKtGTXIZv4Opyfuwfx2fFLL")
                )
                .retrieve()
                .bodyToMono(AuthToken.class)
                .block();
    }
}
