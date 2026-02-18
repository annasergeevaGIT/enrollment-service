package at.enrollment_service.repository;

import at.enrollment_service.model.CourseEnrollment;
import at.enrollment_service.model.EnrollmentStatus;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.*;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

@Repository
@Primary
@Profile("stress")
public class InMemoryCourseEnrollmentRepository
        implements CourseEnrollmentRepository {

    private final Map<Long, CourseEnrollment> storage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong();

    @Override
    public <S extends CourseEnrollment> S save(S entity) {
        long id = idGenerator.incrementAndGet();
        entity.setId(id);
        storage.put(id, entity);
        return entity;
    }

    @Override
    public Page<CourseEnrollment> findAllByCreatedBy(String username, Pageable pageable) {
        List<CourseEnrollment> list = storage.values().stream()
                .filter(e -> username.equals(e.getCreatedBy()))
                .toList();

        return new PageImpl<>(list, pageable, list.size());
    }

    @Override
    public void updateStatusById(Long enrollmentId, EnrollmentStatus newStatus) {
        CourseEnrollment e = storage.get(enrollmentId);
        if (e != null) {
            e.setStatus(newStatus);
        }
    }

    @Override
    public void flush() {

    }

    @Override
    public <S extends CourseEnrollment> S saveAndFlush(S entity) {
        return null;
    }

    @Override
    public <S extends CourseEnrollment> List<S> saveAllAndFlush(Iterable<S> entities) {
        return List.of();
    }

    @Override
    public void deleteAllInBatch(Iterable<CourseEnrollment> entities) {

    }

    @Override
    public void deleteAllByIdInBatch(Iterable<Long> longs) {

    }

    @Override
    public void deleteAllInBatch() {

    }

    @Override
    public CourseEnrollment getOne(Long aLong) {
        return null;
    }

    @Override
    public CourseEnrollment getById(Long aLong) {
        return null;
    }

    @Override
    public CourseEnrollment getReferenceById(Long aLong) {
        return null;
    }

    @Override
    public <S extends CourseEnrollment> Optional<S> findOne(Example<S> example) {
        return Optional.empty();
    }

    @Override
    public <S extends CourseEnrollment> List<S> findAll(Example<S> example) {
        return List.of();
    }

    @Override
    public <S extends CourseEnrollment> List<S> findAll(Example<S> example, Sort sort) {
        return List.of();
    }

    @Override
    public <S extends CourseEnrollment> Page<S> findAll(Example<S> example, Pageable pageable) {
        return null;
    }

    @Override
    public <S extends CourseEnrollment> long count(Example<S> example) {
        return 0;
    }

    @Override
    public <S extends CourseEnrollment> boolean exists(Example<S> example) {
        return false;
    }

    @Override
    public <S extends CourseEnrollment, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        return null;
    }

    @Override
    public <S extends CourseEnrollment> List<S> saveAll(Iterable<S> entities) {
        return List.of();
    }

    @Override
    public Optional<CourseEnrollment> findById(Long aLong) {
        return Optional.empty();
    }

    @Override
    public boolean existsById(Long aLong) {
        return false;
    }

    @Override
    public List<CourseEnrollment> findAll() {
        return List.of();
    }

    @Override
    public List<CourseEnrollment> findAllById(Iterable<Long> longs) {
        return List.of();
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public void deleteById(Long aLong) {

    }

    @Override
    public void delete(CourseEnrollment entity) {

    }

    @Override
    public void deleteAllById(Iterable<? extends Long> longs) {

    }

    @Override
    public void deleteAll(Iterable<? extends CourseEnrollment> entities) {

    }

    @Override
    public void deleteAll() {

    }

    @Override
    public List<CourseEnrollment> findAll(Sort sort) {
        return List.of();
    }

    @Override
    public Page<CourseEnrollment> findAll(Pageable pageable) {
        return null;
    }
}