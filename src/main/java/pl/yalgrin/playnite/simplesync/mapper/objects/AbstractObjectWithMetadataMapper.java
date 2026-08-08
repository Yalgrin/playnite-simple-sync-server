package pl.yalgrin.playnite.simplesync.mapper.objects;

import io.r2dbc.postgresql.codec.Json;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import lombok.SneakyThrows;
import org.apache.commons.lang3.Strings;
import pl.yalgrin.playnite.simplesync.library.domain.LibraryObjectDiffEntity;
import pl.yalgrin.playnite.simplesync.library.domain.LibraryObjectEntity;
import pl.yalgrin.playnite.simplesync.library.dto.LibraryObjectDTO;
import pl.yalgrin.playnite.simplesync.library.dto.LibraryObjectDiffDTO;
import pl.yalgrin.playnite.simplesync.library.dto.LibraryObjectFields;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractObjectWithMetadataMapper<
        E extends LibraryObjectEntity,
        DIFF_E extends LibraryObjectDiffEntity,
        DTO extends LibraryObjectDTO,
        DIFF_DTO extends LibraryObjectDiffDTO> {
    protected final ObjectMapper objectMapper;

    protected AbstractObjectWithMetadataMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Tuple2<E, DIFF_DTO> fillEntityAndGenerateDiff(DTO dto, E target) {
        var diffDTO = createDiffDTO();
        diffDTO.setId(dto.getId());
        diffDTO.setName(dto.getName());
        diffDTO.setChangedFields(new ArrayList<>());
        if (target.getId() == null) {
            diffDTO.getChangedFields().add(LibraryObjectFields.ID);
        }
        if (!Strings.CS.equals(target.getName(), dto.getName())) {
            target.setName(dto.getName());
            diffDTO.getChangedFields().add(LibraryObjectFields.NAME);
        }
        if (target.isRemoved()) {
            target.setRemoved(false);
            target.setPlayniteId(dto.getId());
            diffDTO.setRemoved(dto.isRemoved());
            diffDTO.getChangedFields().add(LibraryObjectFields.REMOVED);
        }
        fillEntityAndGenerateDiffAdditionalFields(dto, target, diffDTO);
        target.setChanged(!diffDTO.getChangedFields().isEmpty());
        return Tuple.of(target, diffDTO);
    }

    protected void fillEntityAndGenerateDiffAdditionalFields(DTO dto, E target, DIFF_DTO diffDTO) {

    }

    public DTO toDTO(E entity) {
        var dto = createDTO();
        dto.setId(entity.getPlayniteId());
        dto.setName(entity.getName());
        fillOtherDtoFields(entity, dto);
        dto.setRemoved(entity.isRemoved());
        return dto;
    }

    protected void fillOtherDtoFields(E entity, DTO dto) {

    }

    @SneakyThrows
    public DIFF_E toEntity(DIFF_DTO dto) {
        var diffEntity = createDiffEntity();
        diffEntity.setPlayniteId(dto.getId());
        diffEntity.setName(dto.getName());
        diffEntity.setDiffData(Json.of(objectMapper.writeValueAsBytes(dto)));
        diffEntity.setRemoved(dto.isRemoved());
        return diffEntity;
    }

    @SneakyThrows
    public DIFF_DTO toDTO(E entity, DIFF_E diffEntity) {
        DIFF_DTO diffDto = objectMapper.readValue(diffEntity.getDiffData().asArray(), getDiffClass());
        List<String> changedFields = diffDto.getChangedFields();
        if (changedFields != null) {
            if (changedFields.contains(LibraryObjectFields.ID)) {
                diffDto.setId(entity.getPlayniteId());
            }
            if (changedFields.contains(LibraryObjectFields.NAME)) {
                diffDto.setName(entity.getName());
            }
            if (changedFields.contains(LibraryObjectFields.REMOVED)) {
                diffDto.setRemoved(entity.isRemoved());
            }
            fillOtherFieldsFromDiffEntity(diffDto, changedFields, entity, diffEntity);
        }
        return diffDto;
    }

    protected void fillOtherFieldsFromDiffEntity(DIFF_DTO diffDto, List<String> changedFields, E entity,
                                                 DIFF_E diffEntity) {

    }

    public Tuple2<E, DIFF_DTO> fillEntityAndGenerateDiff(DIFF_DTO dto, E target) {
        var diffDTO = createDiffDTO();
        diffDTO.setId(dto.getId());
        diffDTO.setName(target.getName());
        fillDiffDtoAdditionalFields(dto, diffDTO, target);
        diffDTO.setChangedFields(new ArrayList<>());
        List<String> changedFields = dto.getChangedFields();
        if (changedFields == null) {
            return Tuple.of(target, diffDTO);
        }
        if (changedFields.contains(LibraryObjectFields.NAME) && !Strings.CS.equals(target.getName(),
                dto.getName())) {
            target.setName(dto.getName());
            diffDTO.setName(dto.getName());
            diffDTO.getChangedFields().add(LibraryObjectFields.NAME);
        }
        if (target.isRemoved()) {
            target.setRemoved(false);
            diffDTO.setRemoved(dto.isRemoved());
            diffDTO.getChangedFields().add(LibraryObjectFields.REMOVED);
        }
        fillEntityAndGenerateDiffAdditionalFields(dto, target, diffDTO);
        target.setChanged(!diffDTO.getChangedFields().isEmpty());
        return Tuple.of(target, diffDTO);
    }

    protected void fillDiffDtoAdditionalFields(DIFF_DTO dto, DIFF_DTO diffDTO, E target) {

    }

    protected void fillEntityAndGenerateDiffAdditionalFields(DIFF_DTO dto, E target, DIFF_DTO diffDTO) {

    }

    protected abstract DTO createDTO();

    protected abstract DIFF_DTO createDiffDTO();

    protected abstract DIFF_E createDiffEntity();

    protected abstract Class<DIFF_DTO> getDiffClass();
}
