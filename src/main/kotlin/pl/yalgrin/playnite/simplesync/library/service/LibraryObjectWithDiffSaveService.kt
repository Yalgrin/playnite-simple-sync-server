package pl.yalgrin.playnite.simplesync.library.service

import org.springframework.http.codec.multipart.FilePart
import pl.yalgrin.playnite.simplesync.library.dto.LibraryObjectDTO
import pl.yalgrin.playnite.simplesync.library.dto.LibraryObjectDiffDTO
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface LibraryObjectWithDiffSaveService<DTO : LibraryObjectDTO, DIFF_DTO : LibraryObjectDiffDTO> :
    LibraryObjectSaveService<DTO> {
    fun saveObjectAndPublishChanges(dto: DTO, fileParts: Flux<FilePart>, saveFiles: Boolean): Mono<DTO>

    fun saveObject(dto: DTO, fileParts: Flux<FilePart>, saveFiles: Boolean): Mono<LibrarySaveResult<DTO>>

    override fun saveObjectAndPublishChanges(dto: DTO): Mono<DTO> =
        saveObjectAndPublishChanges(dto, Flux.empty(), false)

    override fun saveObject(dto: DTO): Mono<LibrarySaveResult<DTO>> =
        saveObject(dto, Flux.empty(), false)

    fun saveObjectDiffAndPublishChanges(diffDto: DIFF_DTO, fileParts: Flux<FilePart>): Mono<DTO>

    fun saveObjectDiff(diffDto: DIFF_DTO, fileParts: Flux<FilePart>): Mono<LibrarySaveResult<DTO>>

    fun findDiffById(id: Long): Mono<DIFF_DTO>
}