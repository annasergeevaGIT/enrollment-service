package at.enrollment_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CourseLineItem {
    private String courseName;
    private BigDecimal price;
    private String language; //private Integer quantity;
}
