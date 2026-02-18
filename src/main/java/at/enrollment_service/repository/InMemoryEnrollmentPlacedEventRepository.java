package at.enrollment_service.repository;

import at.enrollment_service.model.EnrollmentPlacedEvent;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Repository
@Primary
@Profile("stress")
public class InMemoryEnrollmentPlacedEventRepository
        implements EnrollmentPlacedEventRepository {

    private final Map<Long, EnrollmentPlacedEvent> storage = new ConcurrentHashMap<>();

    @Override
    public <S extends EnrollmentPlacedEvent> S save(S entity) {
        storage.put(entity.getEnrollmentId(), entity);
        return entity;
    }

    @Override
    public void flush() {

    }

    @Override
    public <S extends EnrollmentPlacedEvent> S saveAndFlush(S entity) {
        return null;
    }

    @Override
    public <S extends EnrollmentPlacedEvent> List<S> saveAllAndFlush(Iterable<S> entities) {
        return List.of();
    }

    @Override
    public void deleteAllInBatch(Iterable<EnrollmentPlacedEvent> entities) {

    }

    @Override
    public void deleteAllByIdInBatch(Iterable<Long> longs) {

    }

    @Override
    public void deleteAllInBatch() {

    }

    @Override
    public EnrollmentPlacedEvent getOne(Long aLong) {
        return null;
    }

    @Override
    public EnrollmentPlacedEvent getById(Long aLong) {
        return null;
    }

    @Override
    public EnrollmentPlacedEvent getReferenceById(Long aLong) {
        return null;
    }

    @Override
    public <S extends EnrollmentPlacedEvent> Optional<S> findOne(Example<S> example) {
        return Optional.empty();
    }

    @Override
    public <S extends EnrollmentPlacedEvent> List<S> findAll(Example<S> example) {
        return List.of();
    }

    @Override
    public <S extends EnrollmentPlacedEvent> List<S> findAll(Example<S> example, Sort sort) {
        return List.of();
    }

    @Override
    public <S extends EnrollmentPlacedEvent> Page<S> findAll(Example<S> example, Pageable pageable) {
        return null;
    }

    @Override
    public <S extends EnrollmentPlacedEvent> long count(Example<S> example) {
        return 0;
    }

    @Override
    public <S extends EnrollmentPlacedEvent> boolean exists(Example<S> example) {
        return false;
    }

    @Override
    public <S extends EnrollmentPlacedEvent, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        return null;
    }

    @Override
    public <S extends EnrollmentPlacedEvent> List<S> saveAll(Iterable<S> entities) {
        return List.of();
    }

    @Override
    public Optional<EnrollmentPlacedEvent> findById(Long aLong) {
        return Optional.empty();
    }

    @Override
    public boolean existsById(Long aLong) {
        return false;
    }

    @Override
    public List<EnrollmentPlacedEvent> findAll() {
        return List.of();
    }

    @Override
    public List<EnrollmentPlacedEvent> findAllById(Iterable<Long> longs) {
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
    public void delete(EnrollmentPlacedEvent entity) {

    }

    @Override
    public void deleteAllById(Iterable<? extends Long> longs) {

    }

    @Override
    public void deleteAll(Iterable<? extends EnrollmentPlacedEvent> entities) {

    }

    @Override
    public void deleteAll() {

    }

    @Override
    public List<EnrollmentPlacedEvent> findAll(Sort sort) {
        return List.of();
    }

    @Override
    public Page<EnrollmentPlacedEvent> findAll(Pageable pageable) {
        return null;
    }
}
