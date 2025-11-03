package at.enrollment_service.dto;

import lombok.*;

import java.math.BigDecimal;
/*
*   DTO representing course information for enrollment responses to clients.
*/
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@ToString
public class CourseInfo {
    private String name;
    private BigDecimal price;
    private String language;
    private Boolean isAvailable;
}
