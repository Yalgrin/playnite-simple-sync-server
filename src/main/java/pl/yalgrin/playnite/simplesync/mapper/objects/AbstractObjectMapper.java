package pl.yalgrin.playnite.simplesync.mapper.objects;

import org.apache.commons.lang3.StringUtils;
import pl.yalgrin.playnite.simplesync.domain.objects.AbstractObjectEntity;
import pl.yalgrin.playnite.simplesync.dto.objects.AbstractObjectDTO;

public abstract class AbstractObjectMapper<E extends AbstractObjectEntity, DTO extends AbstractObjectDTO> {

    public E fillEntity(DTO dto, E target) {
        target.setChanged(hasChanged(dto, target));
        target.setName(dto.getName());
        target.setRemoved(false);
        fillOtherFields(target, dto);
        return target;
    }

    protected void fillOtherFields(E entity, DTO dto) {

    }

    protected boolean hasChanged(DTO dto, E target) {
        return !StringUtils.equals(target.getName(), dto.getName()) || target.isRemoved();
    }

    public DTO toDTO(E entity) {
        var dto = createDTO();
        dto.setId(entity.getPlayniteId());
        dto.setName(entity.getName());
        dto.setRemoved(entity.isRemoved());
        fillDtoFields(dto, entity);
        return dto;
    }

    protected void fillDtoFields(DTO dto, E entity) {

    }

    protected abstract DTO createDTO();
}
