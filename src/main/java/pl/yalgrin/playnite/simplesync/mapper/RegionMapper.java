package pl.yalgrin.playnite.simplesync.mapper;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import pl.yalgrin.playnite.simplesync.domain.Region;
import pl.yalgrin.playnite.simplesync.dto.RegionDTO;

@Component
public class RegionMapper extends AbstractObjectMapper<Region, RegionDTO> {

    @Override
    protected void fillOtherFields(Region entity, RegionDTO dto) {
        entity.setSpecificationId(dto.getSpecificationId());
    }

    @Override
    protected void fillDtoFields(RegionDTO dto, Region entity) {
        dto.setSpecificationId(entity.getSpecificationId());
    }

    @Override
    protected boolean hasChanged(RegionDTO dto, Region target) {
        return super.hasChanged(dto, target) || !StringUtils.equals(target.getSpecificationId(),
                dto.getSpecificationId());
    }

    @Override
    protected RegionDTO createDTO() {
        return new RegionDTO();
    }
}
