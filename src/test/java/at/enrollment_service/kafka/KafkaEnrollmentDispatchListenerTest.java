package at.enrollment_service.kafka;

import at.EnrollmentDispatchStatus;
import at.EnrollmentDispatchedEvent;
import at.enrollment_service.BaseTest;
import at.enrollment_service.SchemaRegistryContainer;
import at.enrollment_service.model.CourseEnrollment;
import at.enrollment_service.model.EnrollmentStatus;
import at.enrollment_service.repository.CourseEnrollmentRepository;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;


@ActiveProfiles("test")
@SpringBootTest
class KafkaEnrollmentDispatchListenerTest extends BaseTest {


}