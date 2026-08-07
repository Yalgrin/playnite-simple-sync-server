package pl.yalgrin.playnite.simplesync.change.mapper

import org.springframework.stereotype.Component
import pl.yalgrin.playnite.simplesync.change.domain.Change
import pl.yalgrin.playnite.simplesync.dto.ChangeDTO
import java.time.Instant

@Component
class ChangeMapper {
    fun toEntity(dto: ChangeDTO): Change {
        return Change(
            id = dto.id,
            type = dto.type,
            clientId = dto.clientId,
            objectId = dto.objectId,
            notifyAll = dto.isForceFetch,
            createdAt = Instant.now()
        )
    }

    fun toDTO(entity: Change): ChangeDTO {
        val dto = ChangeDTO()
        dto.setId(entity.id)
        dto.type = entity.type
        dto.clientId = entity.clientId
        dto.objectId = entity.objectId
        dto.isForceFetch = entity.notifyAll
        return dto
    }
}