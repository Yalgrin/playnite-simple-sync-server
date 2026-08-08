package pl.yalgrin.playnite.simplesync.mapper.objects;

import org.apache.commons.lang3.Strings;
import pl.yalgrin.playnite.simplesync.library.domain.LibraryObjectEntity;
import pl.yalgrin.playnite.simplesync.library.dto.LibraryObjectDTO;

public abstract class AbstractObjectMapper<E extends LibraryObjectEntity, D extends LibraryObjectDTO> {

    public E fillEntity(D dto, E target) {
        target.setChanged(hasChanged(dto, target));
        target.setName(dto.getName());
        target.setRemoved(false);
        fillOtherFields(target, dto);
        return target;
    }

    protected void fillOtherFields(E entity, D dto) {

    }

    protected boolean hasChanged(D dto, E target) {
        return !Strings.CS.equals(target.getName(), dto.getName()) || target.isRemoved();
    }

    public D toDTO(E entity) {
        var dto = createDTO();
        dto.setId(entity.getPlayniteId());
        dto.setName(entity.getName());
        dto.setRemoved(entity.isRemoved());
        fillDtoFields(dto, entity);
        return dto;
    }

    protected void fillDtoFields(D dto, E entity) {

    }

    protected abstract D createDTO();
}
