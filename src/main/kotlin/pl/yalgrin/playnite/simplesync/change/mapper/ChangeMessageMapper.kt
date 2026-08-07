package pl.yalgrin.playnite.simplesync.change.mapper

import org.springframework.stereotype.Component
import pl.yalgrin.playnite.simplesync.change.domain.Change
import pl.yalgrin.playnite.simplesync.client.message.ChangeMessage
import pl.yalgrin.playnite.simplesync.dto.ChangeDTO

@Component
class ChangeMessageMapper {
    fun toMessage(dto: ChangeDTO): ChangeMessage {
        return ChangeMessage(
            id = dto.id,
            type = dto.type,
            clientId = dto.clientId,
            objectId = dto.objectId,
            forceFetch = dto.isForceFetch
        )
    }

    fun toMessage(entity: Change): ChangeMessage {
        return ChangeMessage(
            id = entity.id,
            type = entity.type,
            clientId = entity.clientId,
            objectId = entity.objectId,
            forceFetch = entity.notifyAll
        )
    }
}