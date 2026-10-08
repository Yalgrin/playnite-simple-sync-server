package pl.yalgrin.playnite.simplesync.panel.mapper

import org.apache.commons.lang3.Strings
import org.springframework.stereotype.Component
import pl.yalgrin.playnite.simplesync.library.domain.Region
import pl.yalgrin.playnite.simplesync.panel.dto.PanelRegionDTO
import reactor.core.publisher.Mono

@Component
class PanelRegionMapper : PanelObjectMapperImpl<Region, PanelRegionDTO>() {
    override fun fillOtherFields(
        entity: Region, dto: PanelRegionDTO
    ): Mono<Region> {
        return Mono.fromSupplier {
            entity.specificationId = dto.specificationId
            entity
        }
    }

    override fun fillOtherDtoFields(
        dto: PanelRegionDTO, entity: Region
    ): Mono<PanelRegionDTO> {
        return Mono.fromSupplier {
            dto.specificationId = entity.specificationId
            dto
        }
    }

    override fun hasChanged(
        dto: PanelRegionDTO, target: Region
    ): Boolean {
        return super.hasChanged(dto, target) || !Strings.CS.equals(
            target.specificationId, dto.specificationId
        )
    }

    override fun createDTO(): PanelRegionDTO = PanelRegionDTO()
}
