package pl.yalgrin.playnite.simplesync.change.mapper

import org.springframework.stereotype.Component
import pl.yalgrin.playnite.simplesync.change.domain.Change
import pl.yalgrin.playnite.simplesync.change.dto.ChangeDTO
import java.time.Instant

@Component
class ChangeMapper {
    fun toEntity(dto: ChangeDTO): Change {
        return Change(
            id = dto.id,
            type = dto.type,
            clientId = dto.clientId!!,
            objectId = dto.objectId,
            diffParentObjectId = dto.diffParentObjectId,
            notifyAll = dto.isForceFetch,
            createdAt = Instant.now()
        )
    }

    fun toDTO(entity: Change): ChangeDTO {
        return ChangeDTO(
            id = entity.id,
            type = entity.type,
            clientId = entity.clientId,
            objectId = entity.objectId,
            diffParentObjectId = entity.diffParentObjectId,
            isForceFetch = entity.notifyAll
        )
    }
}