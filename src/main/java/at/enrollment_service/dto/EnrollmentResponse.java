package at.enrollment_service.dto;

import at.enrollment_service.model.CourseLineItem;
import at.enrollment_service.model.EnrollmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/*
* info about enrollment to be sent to the client
*/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EnrollmentResponse {
    private Long enrollmentId;
    private BigDecimal totalPrice;
    private List<CourseLineItem> courseLineItems;
    private Address address;
    private EnrollmentStatus status;
    private LocalDateTime createdAt;
}
