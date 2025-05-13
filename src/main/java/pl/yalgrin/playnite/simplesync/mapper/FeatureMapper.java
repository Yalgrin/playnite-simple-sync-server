package pl.yalgrin.playnite.simplesync.mapper;

import org.springframework.stereotype.Component;
import pl.yalgrin.playnite.simplesync.domain.Feature;
import pl.yalgrin.playnite.simplesync.dto.FeatureDTO;

@Component
public class FeatureMapper extends AbstractObjectMapper<Feature, FeatureDTO> {

    @Override
    protected FeatureDTO createDTO() {
        return new FeatureDTO();
    }
}
