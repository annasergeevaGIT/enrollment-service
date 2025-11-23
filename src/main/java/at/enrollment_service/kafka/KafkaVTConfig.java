package at.enrollment_service.kafka;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.VirtualThreadTaskExecutor;

@Configuration
public class KafkaVTConfig {

    @Bean(name = "kafkaConsumerExecutor")
    public VirtualThreadTaskExecutor kafkaConsumerExecutor() {
        return new VirtualThreadTaskExecutor("kafka-vt-");
    }
}

