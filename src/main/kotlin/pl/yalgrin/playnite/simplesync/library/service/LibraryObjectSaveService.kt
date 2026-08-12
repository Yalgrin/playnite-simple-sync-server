package pl.yalgrin.playnite.simplesync.library.service

import pl.yalgrin.playnite.simplesync.library.dto.LibraryObjectDTO
import reactor.core.publisher.Mono

interface LibraryObjectSaveService<DTO : LibraryObjectDTO> {
    fun saveObjectAndPublishChanges(dto: DTO): Mono<DTO>

    fun saveObject(dto: DTO): Mono<LibrarySaveResult<DTO>>

    fun deleteObjectAndPublishChanges(dto: DTO): Mono<Unit>

    fun findById(id: Long): Mono<DTO>
}

