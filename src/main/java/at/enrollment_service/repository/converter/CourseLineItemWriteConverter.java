package at.enrollment_service.repository.converter;

import at.enrollment_service.exception.EnrollmentServiceException;
import at.enrollment_service.model.CourseLineItem;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.r2dbc.postgresql.codec.Json;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.http.HttpStatus;

import java.util.List;

/*
 * Send request from server → database
 * Converts List<CourseLineItem> → R2DBC PostgreSQL JSON using Jackson's ObjectMapper.
 * Technical serialization at the persistence layer, used by Spring Data R2DBC during writes.
 * In JPA/Hibernate, this mapping is usually handled automatically, but with R2DBC define it manually.
 */
@WritingConverter
@RequiredArgsConstructor
public class CourseLineItemWriteConverter implements Converter<List<CourseLineItem>, Json> {

    private final ObjectMapper objectMapper;

    @Override
    public Json convert(@NotNull List<CourseLineItem> courseLineItems) {
        try {
            return Json.of(objectMapper.writeValueAsString(courseLineItems));
        } catch (JsonProcessingException e) {
            var msg = String.format("Failed to convert CourseLineItemCollection %s to JSON", courseLineItems);
            throw new EnrollmentServiceException(msg, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
