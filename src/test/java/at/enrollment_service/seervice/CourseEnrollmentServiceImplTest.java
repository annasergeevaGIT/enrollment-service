package at.enrollment_service.seervice;

import at.enrollment_service.BaseIntegrationTest;
import at.enrollment_service.config.EnrollmentServiceProps;
import at.enrollment_service.dto.EnrollmentResponse;
import at.enrollment_service.dto.SortBy;
import at.enrollment_service.exception.EnrollmentServiceException;
import at.enrollment_service.model.CourseLineItem;
import at.enrollment_service.model.EnrollmentStatus;
import at.enrollment_service.service.CourseEnrollmentServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static at.enrollment_service.testdata.TestConstants.*;
import static at.enrollment_service.testdata.TestDataProvider.createEnrollmentRequest;
import static at.enrollment_service.testdata.TestDataProvider.existingItems;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CourseEnrollmentServiceImplTest extends BaseIntegrationTest {

}
