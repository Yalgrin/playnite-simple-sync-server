package pl.yalgrin.playnite.simplesync.service.objects;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.reactive.TransactionalOperator;
import pl.yalgrin.playnite.simplesync.domain.objects.AbstractObjectEntity;
import pl.yalgrin.playnite.simplesync.dto.ChangeDTO;
import pl.yalgrin.playnite.simplesync.dto.objects.AbstractObjectDTO;
import pl.yalgrin.playnite.simplesync.enums.ObjectType;
import pl.yalgrin.playnite.simplesync.mapper.objects.AbstractObjectMapper;
import pl.yalgrin.playnite.simplesync.repository.objects.ObjectRepository;
import pl.yalgrin.playnite.simplesync.service.ChangeListenerService;
import pl.yalgrin.playnite.simplesync.service.ChangeService;
import reactor.core.publisher.Mono;

import java.util.List;

import static pl.yalgrin.playnite.simplesync.utils.ReactorUtils.toMono;

@Service
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractObjectService<E extends AbstractObjectEntity, D extends AbstractObjectDTO> implements
        ObjectSaveService<D> {
    private final ObjectRepository<E> repository;
    private final AbstractObjectMapper<E, D> mapper;
    private final ChangeService changeService;
    private final ChangeListenerService changeListenerService;
    private final TransactionalOperator transactionalOperator;

    public Mono<D> saveObject(D objectDTO, String clientId) {
        return saveObjectWithoutPublishing(objectDTO, clientId)
                .as(transactionalOperator::transactional)
                .flatMap(t -> changeListenerService.publishChange(t._2).thenReturn(t._1));
    }

    public Mono<Tuple2<D, ChangeDTO>> saveObjectWithoutPublishing(D objectDTO, String clientId) {
        return doSaveObject(objectDTO, clientId);
    }

    private Mono<Tuple2<D, ChangeDTO>> doSaveObject(D dto, String clientId) {
        return findOrCreateEntity(dto)
                .map(entity -> mapper.fillEntity(dto, entity))
                .doOnNext(entity -> log.debug("saveObject > entity has{} been changed!",
                        entity.isChanged() ? "" : " not"))
                .filter(AbstractObjectEntity::isChanged)
                .flatMap(repository::save)
                .flatMap(entity -> saveChange(clientId, entity))
                .map(t -> Tuple.of(mapper.toDTO(t._1), t._2));
    }

    private Mono<E> findOrCreateEntity(D dto) {
        return findBasedOnId(dto)
                .switchIfEmpty(findBasedOnName(dto))
                .switchIfEmpty(createNewEntity(dto));
    }

    private Mono<E> findBasedOnId(D dto) {
        return toMono(repository.findByPlayniteId(dto.getId()))
                .doOnNext(
                        e -> log.debug("findOrCreateEntity > found entity with id = {} by playnite id: {}", e.getId(),
                                dto.getId()));
    }

    private Mono<E> findBasedOnName(D dto) {
        return toMono(repository.findByName(dto.getName()))
                .doOnNext(e -> {
                    log.debug("findOrCreateEntity > found entity with id = {} by name: {}", e.getId(),
                            dto.getName());
                    e.setNotifyAll(true);
                });
    }

    private Mono<E> createNewEntity(D dto) {
        return Mono.fromSupplier(() -> createEntityFromDTO(dto))
                .doOnSubscribe(_ -> log.debug("findOrCreateEntity > creating new entity..."));
    }

    protected abstract E createEntityFromDTO(D dto);

    private Mono<Tuple2<E, ChangeDTO>> saveChange(String clientId, E entity) {
        return Mono.justOrEmpty(createChange(clientId, entity))
                .flatMap(changeService::saveChange)
                .map(dto -> Tuple.of(entity, dto));
    }

    private ChangeDTO createChange(String clientId, E entity) {
        return ChangeDTO.builder()
                .type(getObjectType())
                .clientId(clientId)
                .objectId(entity.getId())
                .forceFetch(entity.isNotifyAll())
                .build();
    }

    public Mono<Void> deleteObject(D dto, String clientId) {
        return doDeleteObject(dto, clientId).as(transactionalOperator::transactional)
                .flatMap(changeListenerService::publishChanges);
    }

    private Mono<List<ChangeDTO>> doDeleteObject(D dto, String clientId) {
        return Mono.justOrEmpty(dto)
                .flatMapMany(d -> repository.findByPlayniteIdAndNameAndRemovedIsFalse(d.getId(), d.getName()))
                .map(e -> {
                    log.debug("deleteObject > marking entity with id = {} as removed", e.getId());
                    e.setRemoved(true);
                    return e;
                })
                .flatMap(repository::save)
                .map(e -> createDeleteChange(clientId, e))
                .flatMap(changeService::saveChange)
                .collectList();
    }

    private ChangeDTO createDeleteChange(String clientId, E entity) {
        return ChangeDTO.builder().type(getObjectType())
                .clientId(clientId).objectId(entity.getId())
                .forceFetch(entity.isNotifyAll()).build();
    }

    protected abstract ObjectType getObjectType();

    @Transactional(readOnly = true)
    public Mono<D> findById(Long id) {
        return repository.findById(id)
                .map(mapper::toDTO);
    }
}
