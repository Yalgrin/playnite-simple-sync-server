package pl.yalgrin.playnite.simplesync.library.mapper

import org.apache.commons.lang3.Strings
import org.springframework.stereotype.Component
import pl.yalgrin.playnite.simplesync.library.domain.Region
import pl.yalgrin.playnite.simplesync.library.dto.RegionDTO
import reactor.core.publisher.Mono

@Component
class RegionMapper : LibraryObjectMapperImpl<Region, RegionDTO>() {
    override fun fillOtherFields(
        entity: Region, dto: RegionDTO
    ): Mono<Region> {
        return Mono.fromSupplier {
            entity.specificationId = dto.specificationId
            entity
        }
    }

    override fun fillOtherDtoFields(
        dto: RegionDTO, entity: Region
    ): Mono<RegionDTO> {
        return Mono.fromSupplier {
            dto.specificationId = entity.specificationId
            dto
        }
    }

    override fun hasChanged(
        dto: RegionDTO, target: Region
    ): Boolean {
        return super.hasChanged(dto, target) || !Strings.CS.equals(
            target.specificationId, dto.specificationId
        )
    }

    override fun createDTO(): RegionDTO = RegionDTO()
}