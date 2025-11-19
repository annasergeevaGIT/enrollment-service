package at.enrollment_service;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import javax.sql.DataSource; // Uses blocking JDBC DataSource

import static at.enrollment_service.testdata.TestConstants.DEFAULT_TIMEOUT_STR;

@ActiveProfiles("test")// use application-test.properties
@SqlGroup({ // run sql scripts before and after each test method
        @Sql(
                scripts = "classpath:insert-enrollments.sql",
                executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
        ),
        @Sql(
                scripts = "classpath:delete-enrollments.sql",
                executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
        )
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public abstract class BaseTest {


}