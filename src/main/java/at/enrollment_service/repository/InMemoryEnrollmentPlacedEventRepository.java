package at.enrollment_service.repository;


import at.enrollment_service.model.EnrollmentPlacedEvent;
import org.reactivestreams.Publisher;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Profile("stress")
@Repository
public class InMemoryEnrollmentPlacedEventRepository
        implements EnrollmentPlacedEventRepository {

    private final Map<Long, EnrollmentPlacedEvent> store = new ConcurrentHashMap<>();

    @Override
    public <S extends EnrollmentPlacedEvent> Mono<S> save(S entity) {
        store.put(entity.getId(), entity);
        return Mono.just(entity);
    }

    @Override
    public Mono<EnrollmentPlacedEvent> findById(Long id) {
        return Mono.justOrEmpty(store.get(id));
    }

    @Override
    public Mono<EnrollmentPlacedEvent> findById(Publisher<Long> id) {
        return null;
    }

    @Override
    public Flux<EnrollmentPlacedEvent> findAll() {
        return Flux.fromIterable(store.values());
    }

    @Override public Mono<Boolean> existsById(Long id) { return Mono.just(store.containsKey(id)); }

    @Override
    public Mono<Boolean> existsById(Publisher<Long> id) {
        return null;
    }

    @Override public Mono<Long> count() { return Mono.just((long) store.size()); }
    @Override public Mono<Void> deleteById(Long id) { store.remove(id); return Mono.empty(); }

    @Override
    public Mono<Void> deleteById(Publisher<Long> id) {
        return null;
    }

    @Override public Mono<Void> delete(EnrollmentPlacedEvent entity) { store.remove(entity.getId()); return Mono.empty(); }

    @Override
    public Mono<Void> deleteAllById(Iterable<? extends Long> longs) {
        return null;
    }

    @Override public Mono<Void> deleteAll() { store.clear(); return Mono.empty(); }
    @Override public Flux<EnrollmentPlacedEvent> findAllById(Iterable<Long> ids) {
        return Flux.fromIterable(ids).flatMap(this::findById);
    }

    @Override
    public Flux<EnrollmentPlacedEvent> findAllById(Publisher<Long> idStream) {
        return null;
    }

    @Override public <S extends EnrollmentPlacedEvent> Flux<S> saveAll(Iterable<S> entities) {
        return Flux.fromIterable(entities).flatMap(this::save);
    }
    @Override public <S extends EnrollmentPlacedEvent> Flux<S> saveAll(Publisher<S> entityStream) {
        return Flux.from(entityStream).flatMap(this::save);
    }
    @Override public Mono<Void> deleteAll(Iterable<? extends EnrollmentPlacedEvent> entities) { return Mono.empty(); }
    @Override public Mono<Void> deleteAll(Publisher<? extends EnrollmentPlacedEvent> entityStream) { return Mono.empty(); }
}

