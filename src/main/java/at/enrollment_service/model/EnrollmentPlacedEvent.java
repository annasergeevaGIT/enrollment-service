package at.enrollment_service.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.data.domain.Persistable;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static at.enrollment_service.model.DateUtil.DATE_FORMAT;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "enrollments_outbox")
public class EnrollmentPlacedEvent implements Persistable<Long> {
    @Id
    @Column(name = "enrollment_id")
    private Long enrollmentId;
    @Column(name = "created_by")
    private String createdBy;
    private String city;
    private String street;
    private int house;
    private int apartment;
    @Column(name = "created_at")
    @CreationTimestamp
    @DateTimeFormat(pattern = DATE_FORMAT)
    private LocalDateTime createdAt;

    @Override
    public Long getId() {
        return enrollmentId;
    }

    @Override
    public boolean isNew() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getEntityClass(this) != getEntityClass(o)) return false;
        return enrollmentId != null && enrollmentId.equals(((EnrollmentPlacedEvent) o).enrollmentId);
    }

    @Override
    public final int hashCode() {
        return getEntityClass(this).hashCode();
    }

    public static Class<?> getEntityClass(Object o) {
        return o instanceof HibernateProxy ?
                ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() :
                o.getClass();
    }
}