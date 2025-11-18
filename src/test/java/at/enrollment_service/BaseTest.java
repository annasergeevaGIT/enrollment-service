package at.enrollment_service;

import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import javax.sql.DataSource; // Uses blocking JDBC DataSource

import static at.enrollment_service.testdata.TestConstants.DEFAULT_TIMEOUT_STR;

@ActiveProfiles("test")
public abstract class BaseTest {

    @Autowired
    private DataSource dataSource;

    @Value("classpath:insert-enrollments.sql")
    protected Resource insertEnrollmentsScript;

    @Value("classpath:delete-enrollments.sql")
    protected Resource deleteEnrollmentsScript;

    static final PostgreSQLContainer<?> container = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16.1"))
            .withReuse(true)
            .withDatabaseName("test_database")
            .withUsername("user")
            .withPassword("password")
            .withCommand("-c wal_level=logical -c max_wal_senders=1 -c max_replication_slots=1");

    static {
        container.start();


        System.setProperty("spring.datasource.url", container.getJdbcUrl());
        System.setProperty("spring.datasource.username", container.getUsername());
        System.setProperty("spring.datasource.password", container.getPassword());

        System.setProperty("spring.jpa.hibernate.ddl-auto", "none");

        System.setProperty("spring.flyway.url", container.getJdbcUrl());
        System.setProperty("external.default-timeout", DEFAULT_TIMEOUT_STR);
        System.setProperty("eureka.client.enabled", "false");
        System.setProperty("spring.cloud.loadbalancer.enabled", "false");

        System.setProperty("spring.threads.virtual.enabled", "true");
        System.setProperty("spring.config.import", "optional:configserver:http://localhost:9095");
    }

    /**
     * Data population method. Only run manually or by test classes that need baseline data
     * and do not use @Sql, as @Sql handles its own before-method setup.
     * REMOVED the @BeforeEach annotation to stop double execution.
     */
    void populateDb() {

        executeScriptBlocking(insertEnrollmentsScript);
    }

    /**
     * Cleanup method. Kept @AfterEach to clear data after tests run.
     */
    @AfterEach
    void clearDb() {
        executeScriptBlocking(deleteEnrollmentsScript);
    }

    /**
     * Executes raw SQL scripts synchronously using JDBC.
     */
    protected void executeScriptBlocking(final Resource sqlScript) {
        var populator = new ResourceDatabasePopulator();
        populator.addScript(sqlScript);

        populator.execute(dataSource);
    }
}