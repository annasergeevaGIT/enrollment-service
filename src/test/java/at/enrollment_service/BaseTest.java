package at.enrollment_service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import javax.sql.DataSource;

@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public abstract class BaseTest {

        @Autowired
        private DataSource dataSource;

        @Value("classpath:insert-enrollments.sql")
        private Resource insertEnrollmentsScript;

        @Value("classpath:delete-enrollments.sql")
        private Resource deleteEnrollmentsScript;

        // Manual container definition
        static final PostgreSQLContainer<?> container = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16.1"))
                .withReuse(true)
                .withDatabaseName("test_database")
                .withUsername("user")
                .withPassword("password");

        static {
                container.start();
        }

        @DynamicPropertySource
        static void properties(DynamicPropertyRegistry registry) {
                // 1. Overwrite the URL with the running container's URL
                registry.add("spring.datasource.url", container::getJdbcUrl);
                registry.add("spring.datasource.username", container::getUsername);
                registry.add("spring.datasource.password", container::getPassword);
                registry.add("spring.flyway.url", container::getJdbcUrl);

                // 2. CRITICAL FIX: Force the standard Postgres driver
                // This overrides 'org.testcontainers.jdbc.ContainerDatabaseDriver' from application-test.properties
                registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");

                // 3. Disable Eureka/Cloud stuff in tests
                registry.add("eureka.client.enabled", () -> "false");
                registry.add("spring.cloud.config.enabled", () -> "false");
        }

        @BeforeEach
        void populateDb() {
                executeScript(insertEnrollmentsScript);
        }

        @AfterEach
        void clearDb() {
                executeScript(deleteEnrollmentsScript);
        }

        private void executeScript(Resource sqlScript) {
                var populator = new ResourceDatabasePopulator();
                populator.addScript(sqlScript);
                populator.execute(dataSource);
        }
}