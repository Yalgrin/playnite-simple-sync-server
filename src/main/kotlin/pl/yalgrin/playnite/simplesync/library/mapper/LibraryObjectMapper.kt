package pl.yalgrin.playnite.simplesync.library.mapper

import org.apache.commons.lang3.Strings
import pl.yalgrin.playnite.simplesync.library.domain.LibraryObjectEntity
import pl.yalgrin.playnite.simplesync.library.dto.LibraryObjectDTO
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

interface LibraryObjectMapper<E : LibraryObjectEntity, D : LibraryObjectDTO> {
    fun fillEntity(dto: D, target: E): Mono<E>
    fun toDTO(entity: E): Mono<D>
}

abstract class LibraryObjectMapperImpl<E : LibraryObjectEntity, D : LibraryObjectDTO> : LibraryObjectMapper<E, D> {
    override fun fillEntity(dto: D, target: E): Mono<E> {
        return target.toMono()
            .flatMap { e ->
                hasChangedMono(dto, e).defaultIfEmpty(false)
                    .map { changed ->
                        e.isChanged = changed
                        e
                    }
            }
            .map { fillBasicFields(it, dto) }
            .flatMap { fillOtherFields(it, dto) }
    }

    protected open fun fillBasicFields(entity: E, dto: D): E {
        entity.name = dto.name
        entity.isRemoved = false
        return entity
    }

    protected open fun fillOtherFields(entity: E, dto: D): Mono<E> {
        return entity.toMono()
    }

    protected open fun hasChangedMono(dto: D, target: E): Mono<Boolean> {
        return Mono.fromCallable { hasChanged(dto, target) }
    }

    protected open fun hasChanged(dto: D, target: E): Boolean {
        return !Strings.CS.equals(target.name, dto.name) || target.isRemoved
    }

    override fun toDTO(entity: E): Mono<D> {
        return Mono.fromSupplier { createDTO() }
            .map { fillBasicDtoFields(it, entity) }
            .flatMap { fillOtherDtoFields(it, entity) }
    }

    protected abstract fun createDTO(): D

    private fun fillBasicDtoFields(dto: D, entity: E): D {
        dto.externalId = entity.id
        dto.id = entity.playniteId
        dto.name = entity.name
        dto.isRemoved = entity.isRemoved
        return dto
    }

    protected open fun fillOtherDtoFields(dto: D, entity: E): Mono<D> {
        return dto.toMono()
    }
}