package at.enrollment_service.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table("enrollments") // not jakarta but spring data r2dbc annotation
public class CourseEnrollment {
    @Id // not jakarta but spring data r2dbc annotation
    private Long id;
    @Column("total_price") // not jakarta but spring data r2dbc annotation
    private BigDecimal totalPrice;
    private String city;
    private String street;
    private int house;
    private int apartment;
    @Column("course_line_items")
    private List<CourseLineItem> courseLineItems;
    private EnrollmentStatus status;
    @Column("created_by")
    private String createdBy;
    @Column("created_at")
    @CreatedDate
    private LocalDateTime createdAt;
    @Column("updated_at")
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
