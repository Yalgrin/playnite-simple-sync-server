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
public abstract class AbstractObjectService<E extends AbstractObjectEntity, DTO extends AbstractObjectDTO> implements
        ObjectSaveService<DTO> {
    private final ObjectRepository<E> repository;
    private final AbstractObjectMapper<E, DTO> mapper;
    private final ChangeService changeService;
    private final ChangeListenerService changeListenerService;
    private final TransactionalOperator transactionalOperator;

    public Mono<DTO> saveObject(DTO objectDTO, String clientId) {
        return saveObjectWithoutPublishing(objectDTO, clientId).as(transactionalOperator::transactional)
                .flatMap(t -> changeListenerService.publishChange(t._2).then(Mono.justOrEmpty(t._1)));
    }

    public Mono<Tuple2<DTO, ChangeDTO>> saveObjectWithoutPublishing(DTO objectDTO, String clientId) {
        return doSaveObject(objectDTO, clientId);
    }

    private Mono<Tuple2<DTO, ChangeDTO>> doSaveObject(DTO dto, String clientId) {
        return Mono.justOrEmpty(dto)
                .doOnNext(d -> log.debug("saveObject > START, dto: {}, clientId: {}", d, clientId))
                .flatMap(this::findOrCreateEntity)
                .map(entity -> mapper.fillEntity(dto, entity))
                .doOnNext(e -> log.debug("saveObject > entity has{} been changed!", e.isChanged() ? "" : " not"))
                .filter(AbstractObjectEntity::isChanged)
                .flatMap(repository::save)
                .flatMap(c -> saveChange(clientId, c))
                .map(t -> Tuple.of(mapper.toDTO(t._1), t._2))
                .doOnSuccess(d -> log.debug("saveObject > END, dto: {}", d));
    }

    private Mono<E> findOrCreateEntity(DTO dto) {
        return toMono(repository.findByPlayniteId(dto.getId())).doOnNext(
                        e -> log.debug("findOrCreateEntity > found entity with id = {} by playnite id: {}", e.getId(),
                                dto.getId()))
                .switchIfEmpty(toMono(repository.findByName(dto.getName()))
                        .doOnNext(e -> {
                            log.debug("findOrCreateEntity > found entity with id = {} by name: {}", e.getId(),
                                    dto.getName());
                            e.setNotifyAll(true);
                        }))
                .switchIfEmpty(Mono.fromSupplier(() -> createEntityFromDTO(dto))
                        .doOnNext(e -> log.debug("findOrCreateEntity > creating new entity...")));
    }

    protected abstract E createEntityFromDTO(DTO dto);

    private Mono<Tuple2<E, ChangeDTO>> saveChange(String clientId, E entity) {
        return Mono.justOrEmpty(entity)
                .map(c -> createChange(clientId, c))
                .flatMap(changeService::saveChange)
                .map(dto -> Tuple.of(entity, dto));
    }

    private ChangeDTO createChange(String clientId, E c) {
        return ChangeDTO.builder().type(getObjectType()).clientId(clientId)
                .objectId(c.getId()).forceFetch(c.isNotifyAll()).build();
    }

    public Mono<Void> deleteObject(DTO dto, String clientId) {
        return doDeleteObject(dto, clientId).as(transactionalOperator::transactional)
                .flatMap(changeListenerService::publishChanges);
    }

    private Mono<List<ChangeDTO>> doDeleteObject(DTO dto, String clientId) {
        return Mono.justOrEmpty(dto)
                .doOnNext(d -> log.debug("deleteObject > START, dto: {}, clientId: {}", d, clientId))
                .flatMapMany(d -> repository.findByPlayniteIdAndNameAndRemovedIsFalse(d.getId(), d.getName()))
                .map(e -> {
                    log.debug("deleteObject > marking entity with id = {} as removed", e.getId());
                    e.setRemoved(true);
                    return e;
                })
                .flatMap(repository::save)
                .map(e -> createDeleteChange(clientId, e))
                .flatMap(changeService::saveChange)
                .collectList()
                .doOnSuccess(v -> log.debug("deleteObject > END"));
    }

    private ChangeDTO createDeleteChange(String clientId, E entity) {
        return ChangeDTO.builder().type(getObjectType())
                .clientId(clientId).objectId(entity.getId())
                .forceFetch(entity.isNotifyAll()).build();
    }

    protected abstract ObjectType getObjectType();

    @Transactional(readOnly = true)
    public Mono<DTO> findById(Long id) {
        return Mono.justOrEmpty(id)
                .doOnNext(d -> log.debug("findById > START, id: {}", id))
                .flatMap(repository::findById)
                .map(mapper::toDTO)
                .doOnSuccess(d -> log.debug("findById > END, dto: {}", d));
    }
}
