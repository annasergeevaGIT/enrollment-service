package at.enrollment_service.controller;

import at.enrollment_service.BaseIntegrationTest;
import at.enrollment_service.model.EnrollmentStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static at.enrollment_service.controller.CourseEnrollmentController.USER_HEADER;
import static at.enrollment_service.testdata.TestConstants.*;
import static at.enrollment_service.testdata.TestDataProvider.createEnrollmentInvalidRequest;
import static at.enrollment_service.testdata.TestDataProvider.createEnrollmentRequest;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@EnableAutoConfiguration(exclude = {KafkaAutoConfiguration.class})
public class CourseEnrollmentControllerTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void submitCourseEnrollment_returnsCorrectResponse() throws Exception {
        prepareStubForSuccess();

        var request = createEnrollmentRequest();

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(USER_HEADER, USERNAME_ONE)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.enrollmentId").exists())
                .andExpect(jsonPath("$.status").value(EnrollmentStatus.NEW.name()))
                .andExpect(jsonPath("$.totalPrice").value(SUCCESS_TOTAL_PRICE))
                .andExpect(jsonPath("$.address.city").value(request.getAddress().getCity()))
                .andExpect(jsonPath("$.address.street").value(request.getAddress().getStreet()));
    }

    @Test
    void submitCourseEnrollment_returnsNotFound_whenSomeCoursesAreNotAvailable() throws Exception {
        prepareStubForPartialSuccess();

        var request = createEnrollmentRequest();

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(USER_HEADER, USERNAME_ONE)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getEnrollmentsOfUser_returnsCorrectlySortedList() throws Exception {

        mockMvc.perform(get(BASE_URL)
                        .param("from", "0")
                        .param("size", "10")
                        .param("sortBy", "date_asc")
                        .header(USER_HEADER, USERNAME_ONE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].createdAt").exists());
    }

    @Test
    void submitCourseEnrollment_returnsBadRequest_whenEnrollmentInvalid() throws Exception {
        var invalidRequest = createEnrollmentInvalidRequest();

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(USER_HEADER, USERNAME_ONE)
                        .content(mapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void submitCourseEnrollment_returnsServiceUnavailableOnTimeout() throws Exception {
        prepareStubForSuccessWithTimeout();

        var request = createEnrollmentRequest();

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(USER_HEADER, USERNAME_ONE)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void getEnrollmentsOfUser_returnsBadRequestForInvalidParams() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .param("from", "-1")
                        .param("size", "10")
                        .header(USER_HEADER, USERNAME_ONE))
                .andExpect(status().isBadRequest());
    }
}
