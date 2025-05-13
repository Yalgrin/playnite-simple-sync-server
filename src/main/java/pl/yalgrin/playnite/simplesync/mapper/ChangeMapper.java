
package pl.yalgrin.playnite.simplesync.mapper;

import org.springframework.stereotype.Component;
import pl.yalgrin.playnite.simplesync.domain.Change;
import pl.yalgrin.playnite.simplesync.dto.ChangeDTO;

import java.time.Instant;

@Component
public class ChangeMapper {

    public Change toEntity(ChangeDTO dto) {
        Change entity = new Change();
        entity.setId(dto.getId());
        entity.setType(dto.getType());
        entity.setClientId(dto.getClientId());
        entity.setObjectId(dto.getObjectId());
        entity.setNotifyAll(dto.isForceFetch());
        entity.setCreatedAt(Instant.now());
        return entity;
    }

    public ChangeDTO toDTO(Change entity) {
        ChangeDTO dto = new ChangeDTO();
        dto.setId(entity.getId());
        dto.setType(entity.getType());
        dto.setClientId(entity.getClientId());
        dto.setObjectId(entity.getObjectId());
        dto.setForceFetch(entity.isNotifyAll());
        return dto;
    }
}
