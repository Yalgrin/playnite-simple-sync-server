package pl.yalgrin.playnite.simplesync.service.objects;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import lombok.*;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;
import pl.yalgrin.playnite.simplesync.domain.objects.AbstractObjectDiffEntity;
import pl.yalgrin.playnite.simplesync.domain.objects.AbstractObjectEntity;
import pl.yalgrin.playnite.simplesync.dto.ChangeDTO;
import pl.yalgrin.playnite.simplesync.dto.objects.AbstractDiffDTO;
import pl.yalgrin.playnite.simplesync.dto.objects.AbstractObjectDTO;
import pl.yalgrin.playnite.simplesync.enums.ObjectType;
import pl.yalgrin.playnite.simplesync.exception.ManualSynchronizationRequiredException;
import pl.yalgrin.playnite.simplesync.mapper.objects.AbstractObjectWithMetadataMapper;
import pl.yalgrin.playnite.simplesync.repository.objects.ObjectRepository;
import pl.yalgrin.playnite.simplesync.service.ChangeListenerService;
import pl.yalgrin.playnite.simplesync.service.ChangeService;
import pl.yalgrin.playnite.simplesync.service.MetadataService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import static pl.yalgrin.playnite.simplesync.utils.ReactorUtils.toMono;

@Service
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractObjectWithMetadataService<E extends AbstractObjectEntity, DIFF_E extends AbstractObjectDiffEntity, DTO extends AbstractObjectDTO, DIFF_DTO extends AbstractDiffDTO>
        implements ObjectSaveService<DTO> {
    protected final ObjectRepository<E> repository;
    protected final R2dbcRepository<DIFF_E, Long> diffRepository;
    protected final AbstractObjectWithMetadataMapper<E, DIFF_E, DTO, DIFF_DTO> mapper;
    protected final ChangeService changeService;
    protected final ChangeListenerService changeListenerService;
    protected final MetadataService metadataService;
    protected final ObjectMapper objectMapper;
    protected final TransactionalOperator transactionalOperator;

    @Override
    public Mono<DTO> saveObject(DTO objectDTO, String clientId) {
        return saveObject(objectDTO, clientId, Flux.empty(), false);
    }

    @Override
    public Mono<Tuple2<DTO, ChangeDTO>> saveObjectWithoutPublishing(DTO objectDTO, String clientId) {
        return saveObjectWithoutPublishing(objectDTO, clientId, Flux.empty(), false);
    }

    public Mono<DTO> saveObject(DTO dto, String clientId, Flux<FilePart> fileParts, boolean saveFiles) {
        return saveObjectWithoutPublishing(dto, clientId, fileParts, saveFiles).as(transactionalOperator::transactional)
                .flatMap(t -> changeListenerService.publishChange(t._2).then(Mono.justOrEmpty(t._1)));
    }

    protected Mono<Tuple2<DTO, ChangeDTO>> saveObjectWithoutPublishing(DTO dto, String clientId,
                                                                       Flux<FilePart> fileParts, boolean saveFiles) {
        return Mono.justOrEmpty(dto)
                .flatMap(this::findOrCreateEntity)
                .map(entity -> mapper.fillEntityAndGenerateDiff(dto, entity))
                .flatMap(tuple -> saveEntityAndFiles(fileParts, tuple, saveFiles))
                .flatMap(this::saveDiffIfNeeded)
                .flatMap(c -> saveChange(clientId, c))
                .map(t -> Tuple.of(mapper.toDTO(t._1._1), t._2));
    }

    protected Mono<E> findOrCreateEntity(DTO dto) {
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

    private Mono<List<Boolean>> saveOrDeleteMetadataFiles(Flux<FilePart> fileParts, Tuple2<E, DIFF_DTO> tuple, E e,
                                                          boolean saveFiles) {
        if (!saveFiles) {
            return Mono.empty();
        }
        return Flux.fromIterable(getMetadataFields())
                .flatMap(field -> toMono(fileParts.filter(p -> field.equals(FilenameUtils.getBaseName(p.filename()))))
                        .flatMap(this::readFile)
                        .flatMap(t -> Mono.justOrEmpty((FileData) FileData.builder()
                                .fieldName(field).bytes(t._1)
                                .md5(DigestUtils.md5DigestAsHex(t._1)).filename(t._2)
                                .toSave(true).build()))
                        .switchIfEmpty(Mono.justOrEmpty(FileData.builder().fieldName(field)
                                .toSave(false).build())))
                .flatMap(t -> {
                    if (t.isToSave()) {
                        if (shouldSaveMetadata(e, t.getBytes(), t.getMd5(), t.getFieldName())) {
                            return metadataService.saveMetadata(getMetadataFolder(), getIdPart(e), t.getFilename(),
                                            t.getBytes(), t.getFieldName())
                                    .doOnNext(_ -> {
                                        setHex(e, t.getFieldName(), t.getMd5());
                                        tuple._2.getChangedFields().add(t.getFieldName());
                                    });
                        } else {
                            return Mono.justOrEmpty(true).doOnNext(
                                            b -> log.debug("saveOrDeleteMetadataFiles > skipping file: {}", t.getFieldName()))
                                    .then(metadataService.deleteExcessiveMetadata(getMetadataFolder(), getIdPart(e),
                                            t.getFilename(), t.getFieldName()));
                        }
                    } else {
                        return metadataService.deleteMetadata(getMetadataFolder(), getIdPart(e), t.getFieldName())
                                .doOnNext(_ -> {
                                    setHex(e, t.getFieldName(), t.getMd5());
                                    tuple._2.getChangedFields().add(t.getFieldName());
                                });
                    }
                })
                .collectList();
    }

    private Mono<E> saveEntityIfNeeded(Tuple2<E, DIFF_DTO> tuple) {
        return Mono.justOrEmpty(tuple._1)
                .flatMap(e -> {
                    if (e.getId() == null) {
                        return repository.save(e);
                    } else {
                        return Mono.justOrEmpty(e);
                    }
                });
    }

    private Mono<Tuple2<E, DIFF_DTO>> saveEntityAndFiles(Flux<FilePart> fileParts, Tuple2<E, DIFF_DTO> tuple,
                                                         boolean saveFiles) {
        return saveEntityIfNeeded(tuple)
                .flatMap(e -> saveOrDeleteMetadataFiles(fileParts, tuple, e, saveFiles).then(saveEntityIfChanged(e)))
                .then(Mono.justOrEmpty(tuple));
    }

    private Mono<E> saveEntityIfChanged(E entity) {
        return Mono.justOrEmpty(entity)
                .doOnNext(e -> log.debug("saveObject > entity has{} been changed!", e.isChanged() ? "" : " not"))
                .filter(AbstractObjectEntity::isChanged)
                .flatMap(repository::save)
                .switchIfEmpty(Mono.justOrEmpty(entity));
    }

    protected abstract Set<String> getMetadataFields();

    protected Mono<Tuple2<byte[], String>> readFile(FilePart filePart) {
        return DataBufferUtils.join(filePart.content())
                .map(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);
                    return bytes;
                })
                .map(bytes -> Tuple.of(bytes, filePart.filename()));
    }

    public Mono<DTO> saveObjectDiff(DIFF_DTO diffDto, String clientId, Flux<FilePart> fileParts) {
        return saveObjectDiffWithoutPublishing(diffDto, clientId, fileParts).as(transactionalOperator::transactional)
                .flatMap(t -> changeListenerService.publishChange(t._2).then(Mono.justOrEmpty(t._1)));
    }

    protected Mono<Tuple2<DTO, ChangeDTO>> saveObjectDiffWithoutPublishing(DIFF_DTO diffDto, String clientId,
                                                                           Flux<FilePart> fileParts) {
        return Mono.justOrEmpty(diffDto)
                .doOnNext(dto -> log.debug("saveObjectDiff > START, dto: {}, clientId: {}", dto, clientId))
                .flatMap(this::findOrCreateEntity)
                .map(entity -> mapper.fillEntityAndGenerateDiff(diffDto, entity))
                .flatMap(tuple -> saveEntityAndFilesFromDiff(diffDto, fileParts, tuple))
                .flatMap(this::saveDiffIfNeeded)
                .flatMap(c -> saveChange(clientId, c))
                .map(t -> Tuple.of(mapper.toDTO(t._1._1), t._2))
                .doOnSuccess(dto -> log.debug("saveObjectDiff > END, dto: {}", dto));
    }

    protected Mono<E> findOrCreateEntity(DIFF_DTO dto) {
        return toMono(repository.findByPlayniteId(dto.getId())).doOnNext(
                        e -> log.debug("findOrCreateEntity > found entity with id = {} by playnite id: {}", e.getId(),
                                dto.getId()))
                .switchIfEmpty(
                        Mono.error(new ManualSynchronizationRequiredException("Manual synchronization required!")));
    }

    private Mono<Tuple2<E, DIFF_DTO>> saveEntityAndFilesFromDiff(DIFF_DTO diffDto, Flux<FilePart> fileParts,
                                                                 Tuple2<E, DIFF_DTO> tuple) {
        List<String> changedFields = diffDto.getChangedFields();
        if (CollectionUtils.isEmpty(changedFields)) {
            return Mono.justOrEmpty(tuple).doOnNext(t -> log.debug("saveEntityAndFilesFromDiff > no changed fields"));
        }
        E e = tuple._1;
        return saveOrDeleteMetadataFilesFromDiff(fileParts, tuple, changedFields, e)
                .then(saveEntityIfChanged(e))
                .then(Mono.justOrEmpty(tuple));
    }

    private Mono<List<Boolean>> saveOrDeleteMetadataFilesFromDiff(Flux<FilePart> fileParts, Tuple2<E, DIFF_DTO> tuple,
                                                                  List<String> changedFields, E e) {
        return Flux.fromIterable(changedFields)
                .filter(str -> getMetadataFields().contains(str))
                .flatMap(field -> toMono(fileParts.filter(p -> field.equals(FilenameUtils.getBaseName(p.filename())))
                        .flatMap(this::readFile)
                        .flatMap(t -> Mono.justOrEmpty((FileData) FileData.builder()
                                .fieldName(field).bytes(t._1)
                                .md5(DigestUtils.md5DigestAsHex(t._1)).filename(t._2)
                                .toSave(true).build()))
                        .switchIfEmpty(Mono.justOrEmpty(FileData.builder().fieldName(field)
                                .toSave(false).build()))))
                .flatMap(t -> {
                    if (t.isToSave()) {
                        if (shouldSaveMetadata(e, t.getBytes(), t.getMd5(), t.getFieldName())) {
                            return metadataService.saveMetadata(getMetadataFolder(), getIdPart(e), t.getFilename(),
                                            t.getBytes(), t.getFieldName())
                                    .doOnNext(_ -> {
                                        setHex(e, t.getFieldName(), t.getMd5());
                                        tuple._2.getChangedFields().add(t.getFieldName());
                                    });
                        } else {
                            return Mono.just(true)
                                    .doOnNext(_ -> log.debug("saveEntityAndFilesFromDiff > skipping file: {}",
                                            t.getFieldName()))
                                    .then(metadataService.deleteExcessiveMetadata(getMetadataFolder(), getIdPart(e),
                                            t.getFilename(), t.getFieldName()));
                        }
                    } else {
                        return metadataService.deleteMetadata(getMetadataFolder(), getIdPart(e), t.getFieldName())
                                .doOnSuccess(_ -> {
                                    setHex(e, t.getFieldName(), t.getMd5());
                                    tuple._2.getChangedFields().add(t.getFieldName());
                                });
                    }
                })
                .collectList();
    }

    protected abstract void setHex(E e, String baseName, String md5);

    protected abstract boolean shouldSaveMetadata(E e, byte[] bytes, String md5, String filename);

    private Mono<Tuple2<E, DIFF_E>> saveDiffIfNeeded(Tuple2<E, DIFF_DTO> t) {
        return Mono.justOrEmpty(t._2)
                .filter(dto -> dto != null && dto.getChangedFields() != null && !dto.getChangedFields().isEmpty())
                .doOnNext(dto -> dto.setBaseObjectId(t._1.getId()))
                .doOnNext(dto -> log.debug("saveDiffIfNeeded > diffDTO: {}", dto))
                .map(mapper::toEntity)
                .flatMap(diffRepository::save)
                .map(e -> Tuple.of(t._1, e));
    }

    protected abstract String getMetadataFolder();

    protected String getIdPart(E e) {
        return e.getId() + "";
    }


    protected abstract E createEntityFromDTO(DTO dto);

    private Mono<Tuple2<Tuple2<E, DIFF_E>, ChangeDTO>> saveChange(String clientId, Tuple2<E, DIFF_E> category) {
        return Mono.justOrEmpty(category)
                .mapNotNull(c -> createChange(clientId, c))
                .filter(Objects::nonNull)
                .flatMap(changeService::saveChange)
                .map(dto -> Tuple.of(category, dto));
    }

    private ChangeDTO createChange(String clientId, Tuple2<E, DIFF_E> tuple) {
        E entity = tuple._1;
        DIFF_E diffE = tuple._2;
        if (isSavingDiffForEntireEntity(entity, diffE)) {
            return ChangeDTO.builder().type(getObjectType()).clientId(clientId)
                    .objectId(entity.getId()).forceFetch(entity.isNotifyAll()).build();
        }
        if (diffE == null) {
            return null;
        }
        return ChangeDTO.builder().type(getDiffType()).clientId(clientId)
                .objectId(diffE.getId()).forceFetch(entity.isNotifyAll()).build();
    }

    @SneakyThrows
    private boolean isSavingDiffForEntireEntity(E entity, DIFF_E diffE) {
        if (entity.isChanged() && diffE == null) {
            return true;
        }
        if (diffE == null) {
            return false;
        }
        DIFF_DTO diffDTO = objectMapper.readValue(diffE.getDiffData().asArray(), getDiffDtoClass());
        return diffDTO != null && (diffDTO.getChangedFields().contains("Id") || diffDTO.getChangedFields()
                .contains("Removed"));
    }

    protected abstract Class<DIFF_DTO> getDiffDtoClass();

    public Mono<Void> deleteObject(DTO dto, String clientId) {
        return doDeleteObject(dto, clientId).as(transactionalOperator::transactional).flatMap(
                changeListenerService::publishChanges);
    }

    private Mono<List<ChangeDTO>> doDeleteObject(DTO dto, String clientId) {
        return Mono.justOrEmpty(dto)
                .doOnNext(d -> log.debug("deleteObject > START, dto: {}, clientId: {}", dto, clientId))
                .flatMapMany(this::findObjectToDelete)
                .map(e -> {
                    log.debug("deleteObject > marking entity with id = {} as removed", e.getId());
                    e.setRemoved(true);
                    return e;
                })
                .flatMap(repository::save)
                .flatMap(e -> changeService.saveChange(createDeleteChange(clientId, e)))
                .collectList()
                .doOnSuccess(d -> log.debug("deleteObject > END"));
    }

    protected Flux<E> findObjectToDelete(DTO categoryDTO) {
        return repository.findByPlayniteIdAndNameAndRemovedIsFalse(categoryDTO.getId(), categoryDTO.getName());
    }

    private ChangeDTO createDeleteChange(String clientId, E entity) {
        return ChangeDTO.builder().type(getObjectType())
                .clientId(clientId).objectId(entity.getId())
                .forceFetch(entity.isNotifyAll()).build();
    }

    protected abstract ObjectType getObjectType();

    protected abstract ObjectType getDiffType();

    @Transactional(readOnly = true)
    public Mono<DTO> findById(Long id) {
        return repository.findById(id).map(mapper::toDTO);
    }

    @Transactional(readOnly = true)
    public Mono<DIFF_DTO> findDiffById(Long id) {
        return diffRepository.findById(id).flatMap(
                diff -> toMono(repository.findByPlayniteId(diff.getPlayniteId())).map(e -> mapper.toDTO(e, diff)));
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @SuperBuilder
    private static class FileData {
        private String fieldName;
        private byte[] bytes;
        private String md5;
        private String filename;
        private boolean toSave;
    }
}
