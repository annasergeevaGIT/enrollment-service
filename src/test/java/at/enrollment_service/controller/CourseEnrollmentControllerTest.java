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

}
