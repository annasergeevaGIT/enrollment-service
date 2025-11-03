package at.enrollment_service.config;

import at.enrollment_service.repository.converter.CourseLineItemReadConverter;
import at.enrollment_service.repository.converter.CourseLineItemWriteConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.data.r2dbc.dialect.PostgresDialect;

import java.util.List;

@Configuration
@EnableR2dbcAuditing //Enables automatic handling of fields createdAt and updatedAt
public class R2dbcConfig {
    /*
     * Register custom converters for R2DBC to handle conversion between PostgreSQL JSON and List<CourseLineItem>.
     * This is necessary because R2DBC does not automatically handle complex types like JSON.
     * The converters use Jackson's ObjectMapper for serialization and deserialization.
     */
    @Bean
    public R2dbcCustomConversions r2dbcCustomConversions(ObjectMapper objectMapper) {// ObjectMapper injected so converters use Jackson to handle JSON de- and serialization
        List<Converter<?,?>> converters = List.of(
                new CourseLineItemReadConverter(objectMapper),
                new CourseLineItemWriteConverter(objectMapper)
        );
        return R2dbcCustomConversions.of(PostgresDialect.INSTANCE, converters);
    }
}
