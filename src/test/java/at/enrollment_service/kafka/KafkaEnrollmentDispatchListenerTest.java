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

    public static final String CONFLUENT_VERSION = "7.5.2";

    private static final String ENROLLMENT_DISPATCH_TOPIC = "v1.enrollments_dispatch";

    private static final Network NETWORK = Network.newNetwork();

    public static final KafkaContainer KAFKA =
            new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.2"))
                    .withKraft()
                    .withNetwork(NETWORK);

    public static final SchemaRegistryContainer SCHEMA_REGISTRY =
            new SchemaRegistryContainer(CONFLUENT_VERSION);

    @BeforeAll
    static void setup() {
        KAFKA.start();
        SCHEMA_REGISTRY.withKafka(KAFKA).start();

        System.setProperty("spring.kafka.bootstrap-servers", KAFKA.getBootstrapServers());
        System.setProperty("spring.kafka.consumer.properties.schema.registry.url",
                "http://localhost:" + SCHEMA_REGISTRY.getFirstMappedPort());
        System.setProperty("spring.kafka.producer.key-serializer",
                "org.apache.kafka.common.serialization.StringSerializer");
        System.setProperty("spring.kafka.producer.value-serializer",
                "io.confluent.kafka.serializers.KafkaAvroSerializer");
        System.setProperty("spring.kafka.producer.properties.schema.registry.url",
                "http://localhost:" + SCHEMA_REGISTRY.getFirstMappedPort());
    }

    @Autowired
    private KafkaTemplate<String, EnrollmentDispatchedEvent> kafkaTemplate;

    @Autowired
    private CourseEnrollmentRepository courseEnrollmentRepository;

    @Test
    void consumeEnrollmentDispatchEvent_consumesEventAndUpdatesCourseEnrollmentStatus() throws Exception {

        CourseEnrollment enrollment =
                courseEnrollmentRepository.findAll().stream().findFirst().orElseThrow();

        assertThat(enrollment.getStatus()).isEqualTo(EnrollmentStatus.NEW);

        EnrollmentDispatchedEvent event = EnrollmentDispatchedEvent.newBuilder()
                .setEnrollmentId(enrollment.getId())
                .setStatus(EnrollmentDispatchStatus.ACCEPTED)
                .build();

        kafkaTemplate.send(ENROLLMENT_DISPATCH_TOPIC,
                String.valueOf(enrollment.getId()), event).get();

        Awaitility.await()
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    CourseEnrollment updated =
                            courseEnrollmentRepository.findById(enrollment.getId())
                                    .orElseThrow();

                    assertThat(updated.getStatus())
                            .isEqualTo(EnrollmentStatus.ACCEPTED);
                });
    }
}