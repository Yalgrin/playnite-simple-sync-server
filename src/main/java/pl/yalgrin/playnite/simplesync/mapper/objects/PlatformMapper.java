package pl.yalgrin.playnite.simplesync.mapper.objects;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.yalgrin.playnite.simplesync.domain.objects.Platform;
import pl.yalgrin.playnite.simplesync.domain.objects.PlatformDiff;
import pl.yalgrin.playnite.simplesync.dto.objects.PlatformDTO;
import pl.yalgrin.playnite.simplesync.dto.objects.PlatformDiffDTO;

import java.util.List;

@Component
public class PlatformMapper extends
        AbstractObjectWithMetadataMapper<Platform, PlatformDiff, PlatformDTO, PlatformDiffDTO> {

    @Autowired
    public PlatformMapper(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    protected void fillEntityAndGenerateDiffAdditionalFields(PlatformDTO dto, Platform target,
                                                             PlatformDiffDTO diffDTO) {
        if (!StringUtils.equals(target.getSpecificationId(), dto.getSpecificationId())) {
            target.setSpecificationId(dto.getSpecificationId());
            diffDTO.setSpecificationId(dto.getSpecificationId());
            diffDTO.getChangedFields().add("SpecificationId");
        }
    }

    @Override
    protected void fillEntityAndGenerateDiffAdditionalFields(PlatformDiffDTO dto, Platform target,
                                                             PlatformDiffDTO diffDTO) {
        List<String> changedFields = dto.getChangedFields();
        if (changedFields == null) {
            return;
        }
        if (changedFields.contains("SpecificationId") && !StringUtils.equals(target.getSpecificationId(),
                dto.getSpecificationId())) {
            target.setSpecificationId(dto.getSpecificationId());
            diffDTO.setSpecificationId(dto.getSpecificationId());
            diffDTO.getChangedFields().add("SpecificationId");
        }
    }

    @Override
    protected void fillOtherDtoFields(Platform entity, PlatformDTO dto) {
        dto.setSpecificationId(entity.getSpecificationId());
        dto.setHasIcon(entity.getIconMd5() != null);
        dto.setHasCoverImage(entity.getCoverImageMd5() != null);
        dto.setHasBackgroundImage(entity.getBackgroundImageMd5() != null);
    }

    @Override
    protected void fillOtherFieldsFromDiffEntity(PlatformDiffDTO platformDiffDTO, List<String> changedFields,
                                                 Platform entity, PlatformDiff diffEntity) {
        if (changedFields.contains("SpecificationId")) {
            platformDiffDTO.setSpecificationId(entity.getSpecificationId());
        }
    }

    @Override
    protected PlatformDTO createDTO() {
        return new PlatformDTO();
    }

    @Override
    protected PlatformDiffDTO createDiffDTO() {
        return new PlatformDiffDTO();
    }

    @Override
    protected PlatformDiff createDiffEntity() {
        return new PlatformDiff();
    }

    @Override
    protected Class<PlatformDiffDTO> getDiffClass() {
        return PlatformDiffDTO.class;
    }
}
