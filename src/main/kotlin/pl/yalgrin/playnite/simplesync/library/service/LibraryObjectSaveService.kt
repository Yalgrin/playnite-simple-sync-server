package pl.yalgrin.playnite.simplesync.library.service

import pl.yalgrin.playnite.simplesync.library.dto.LibraryObjectDTO
import pl.yalgrin.playnite.simplesync.library.dto.PanelObjectDTO
import reactor.core.publisher.Mono

interface LibraryObjectSaveService<DTO : LibraryObjectDTO, PANEL_DTO : PanelObjectDTO> {
    fun saveObjectAndPublishChanges(dto: DTO): Mono<DTO>

    fun saveObject(dto: DTO): Mono<LibrarySaveResult<DTO>>

    fun deleteObjectAndPublishChanges(dto: DTO): Mono<Unit>

    fun findById(id: Long): Mono<DTO>

    fun saveObjectAndPublishChanges(dto: PANEL_DTO): Mono<PANEL_DTO>

    fun saveObject(dto: PANEL_DTO): Mono<LibrarySaveResult<PANEL_DTO>>

    fun deleteObjectAndPublishChanges(dto: PANEL_DTO): Mono<Unit>

    fun findByIdForPanel(id: Long): Mono<PANEL_DTO>

    fun findAllForPanel(): Mono<List<PANEL_DTO>>

    fun findByPlayniteIdForPanel(playniteId: String): Mono<PANEL_DTO>

    fun findByPlayniteIdsForPanel(playniteIds: Collection<String>): Mono<List<PANEL_DTO>>
}

