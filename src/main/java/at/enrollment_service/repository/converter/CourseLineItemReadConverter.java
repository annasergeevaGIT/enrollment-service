package at.enrollment_service.repository.converter;

import at.enrollment_service.exception.EnrollmentServiceException;
import at.enrollment_service.model.CourseLineItem;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.r2dbc.postgresql.codec.Json;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.List;

/*
 * Receive response from database → server
 * Converts PostgreSQL JSON column values into Java objects when reading from the database.
 * Technical deserialization using Jackson's ObjectMapper at the persistence layer, used by: Spring Data R2DBC
 * In JPA/Hibernate, this mapping is usually handled automatically, but with R2DBC define it manually.
*/
@ReadingConverter
@RequiredArgsConstructor
public class CourseLineItemReadConverter implements Converter<Json, List<CourseLineItem>> {

    private final ObjectMapper objectMapper;

    @Override
    public List<CourseLineItem> convert(Json source) {
        try {
            return objectMapper.readValue(source.asArray(), new TypeReference<List<CourseLineItem>>() {});
        } catch (IOException e) {
            var msg = String.format("Failed to convert JSON %s to List<CourseLineItem>", source.asString());
            throw new EnrollmentServiceException(msg, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
