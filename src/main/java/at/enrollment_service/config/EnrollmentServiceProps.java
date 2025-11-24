package at.enrollment_service.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
/*
* Configuration properties for external services used by the Enrollment Service.
* initialized by application.properties with prefix "external"
 */
@Data
@AllArgsConstructor

@ConfigurationProperties(prefix = "external") //scanned by @ConfigurationPropertiesScan
public class EnrollmentServiceProps {
    private final String courseServiceUrl;
    private final String courseInfoPath;
    private final Duration defaultTimeout; //
    private final Duration retryBackoff; //
    private final int retryCount;
    private final double retryJitter; //
}
