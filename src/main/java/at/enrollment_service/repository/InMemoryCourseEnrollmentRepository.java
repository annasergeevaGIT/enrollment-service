package at.enrollment_service.repository;


import at.enrollment_service.model.CourseEnrollment;
import at.enrollment_service.model.EnrollmentStatus;
import org.reactivestreams.Publisher;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Profile("stress")
@Repository
public class InMemoryCourseEnrollmentRepository implements CourseEnrollmentRepository {

    private final Map<Long, CourseEnrollment> store = new ConcurrentHashMap<>();
    private final AtomicLong idGen = new AtomicLong();

    @Override
    public <S extends CourseEnrollment> Mono<S> save(S entity) {
        if (entity.getId() == null) {
            entity.setId(idGen.incrementAndGet());
        }
        store.put(entity.getId(), entity);
        return Mono.just(entity);
    }

    @Override
    public <S extends CourseEnrollment> Flux<S> saveAll(Iterable<S> entities) {
        return null;
    }

    @Override
    public <S extends CourseEnrollment> Flux<S> saveAll(Publisher<S> entityStream) {
        return null;
    }

    @Override
    public Mono<CourseEnrollment> findById(Long id) {
        return Mono.justOrEmpty(store.get(id));
    }

    @Override
    public Mono<CourseEnrollment> findById(Publisher<Long> id) {
        return null;
    }

    @Override
    public Mono<Boolean> existsById(Long aLong) {
        return null;
    }

    @Override
    public Mono<Boolean> existsById(Publisher<Long> id) {
        return null;
    }

    @Override
    public Flux<CourseEnrollment> findAll() {
        return Flux.fromIterable(store.values());
    }

    @Override
    public Flux<CourseEnrollment> findAllById(Iterable<Long> longs) {
        return null;
    }

    @Override
    public Flux<CourseEnrollment> findAllById(Publisher<Long> idStream) {
        return null;
    }

    @Override
    public Mono<Long> count() {
        return null;
    }

    @Override
    public Mono<Void> deleteById(Long id) {
        store.remove(id);
        return Mono.empty();
    }

    @Override
    public Mono<Void> deleteById(Publisher<Long> id) {
        return null;
    }

    @Override
    public Mono<Void> delete(CourseEnrollment entity) {
        return null;
    }

    @Override
    public Mono<Void> deleteAllById(Iterable<? extends Long> longs) {
        return null;
    }

    @Override
    public Mono<Void> deleteAll(Iterable<? extends CourseEnrollment> entities) {
        return null;
    }

    @Override
    public Mono<Void> deleteAll(Publisher<? extends CourseEnrollment> entityStream) {
        return null;
    }

    @Override
    public Mono<Void> deleteAll() {
        return null;
    }

    @Override
    public Flux<CourseEnrollment> findAllByCreatedBy(String username, Pageable pageable) {
        return Flux.fromStream(
                store.values().stream()
                        .filter(e -> username.equals(e.getCreatedBy()))
                        .skip(pageable.getOffset())
                        .limit(pageable.getPageSize())
        );
    }

    @Override
    public Mono<Void> updateStatusById(Long enrollmentId, EnrollmentStatus newStatus) {
        CourseEnrollment e = store.get(enrollmentId);
        if (e != null) {
            e.setStatus(newStatus);
        }
        return Mono.empty();
    }
}
