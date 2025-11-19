package at.enrollment_service.model;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.Hibernate;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity // JPA annotation
@Table(name ="enrollments") // not jakarta but spring data r2dbc annotation
@EntityListeners(AuditingEntityListener.class)
public class CourseEnrollment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name ="total_price")
    private BigDecimal totalPrice;
    private String city;
    private String street;
    private int house;
    private int apartment;

    @Column(name ="course_line_items", columnDefinition = "jsonb")
    @Type(JsonBinaryType.class)
    private List<CourseLineItem> courseLineItems;

    @Enumerated(EnumType.STRING)
    private EnrollmentStatus status;

    @Column(name ="created_by")
    private String createdBy;

    @Column(name ="created_at")
    @CreationTimestamp
    @DateTimeFormat(pattern = DateUtil.DATE_FORMAT)
    private LocalDateTime createdAt;

    @Column(name ="updated_at")
    @UpdateTimestamp
    @DateTimeFormat(pattern = DateUtil.DATE_FORMAT)
    private LocalDateTime updatedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        return getId() != null && getId().equals(((CourseEnrollment) o).getId());
    }

    @Override
    public final int hashCode() {
        return Hibernate.getClass(this).hashCode();
    }
}

