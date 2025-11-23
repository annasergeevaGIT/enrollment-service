package at.enrollment_service;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

@ActiveProfiles("test")
@ImportAutoConfiguration(exclude = R2dbcAutoConfiguration.class)
@SqlGroup({
        @Sql(
                scripts = "classpath:insert-enrollments.sql",
                executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
        ),
        @Sql(
                scripts = "classpath:delete-enrollments.sql",
                executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
        )
})
public abstract class BaseTest {

}

