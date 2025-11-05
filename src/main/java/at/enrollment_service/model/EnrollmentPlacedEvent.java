package at.enrollment_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Table("enrollments_outbox")
public class EnrollmentPlacedEvent implements Persistable<Long> {
    @Id
    @Column("enrollment_id")
    private Long enrollmentId;
    @Column("created_by")
    private String createdBy;
    private String city;
    private String street;
    private int house;
    private int apartment;
    @Column("created_at")
    private LocalDateTime createdAt;

    @Override
    public Long getId() {
        return enrollmentId;
    }

    @Override
    public boolean isNew() {
        return true;
    }
}