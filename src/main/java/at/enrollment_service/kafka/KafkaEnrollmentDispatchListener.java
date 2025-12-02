package at.enrollment_service.kafka;

import at.EnrollmentDispatchedEvent;
import at.enrollment_service.model.EnrollmentStatus;
import at.enrollment_service.repository.CourseEnrollmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaEnrollmentDispatchListener {

    private final CourseEnrollmentRepository courseEnrollmentRepository;
    @Value("${kafkaprops.nack-sleep-duration}")
    private Duration nackSleepDuration;

    @KafkaListener(topics = {"${kafkaprops.enrollment-dispatch-topic}"})
    public void consumeEnrollmentDispatchEvent(EnrollmentDispatchedEvent event,
                                          @Header(KafkaHeaders.RECEIVED_KEY) String key,
                                          @Header(KafkaHeaders.RECEIVED_PARTITION) Integer partition,
                                          @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                          Acknowledgment acknowledgment) {
        log.info("Received EnrollmentDispatchedEvent from Kafka: {}. Key: {}. Partition: {}. Topic: {}", event, key, partition, topic);
        try {
            courseEnrollmentRepository
                    .updateStatusById(event.getEnrollmentId(), EnrollmentStatus.fromString(event.getStatus().name()))
                    .block();
            log.info("Successfully updated EnrollmentStatus to {} for enrollment with ID={}", event.getStatus(), event.getEnrollmentId());
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Failed to update EnrollmentStatus to {} for enrollment with ID={}", event.getStatus(), event.getEnrollmentId());
            // don't acknowledge
            acknowledgment.nack(nackSleepDuration);
        }
    }
}