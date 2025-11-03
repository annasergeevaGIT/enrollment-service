package at.enrollment_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

@Data
@AllArgsConstructor
public class GetCourseInfoRequest {
    private Set<String> courseNames;
}
