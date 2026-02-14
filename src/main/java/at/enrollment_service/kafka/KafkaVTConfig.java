package at.enrollment_service.kafka;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.VirtualThreadTaskExecutor;

@Configuration
//@ConditionalOnProperty(
//        name = "app.kafka.enabled",
//        havingValue = "true",
//        matchIfMissing = true
//)
public class KafkaVTConfig {

    @Bean(name = "kafkaConsumerExecutor")
    public VirtualThreadTaskExecutor kafkaConsumerExecutor() {
        return new VirtualThreadTaskExecutor("kafka-vt-");
    }
}

