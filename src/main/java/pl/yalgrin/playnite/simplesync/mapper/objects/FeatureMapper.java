package pl.yalgrin.playnite.simplesync.mapper.objects;

import org.springframework.stereotype.Component;
import pl.yalgrin.playnite.simplesync.library.domain.Feature;
import pl.yalgrin.playnite.simplesync.library.dto.FeatureDTO;

@Component
public class FeatureMapper extends AbstractObjectMapper<Feature, FeatureDTO> {

    @Override
    protected FeatureDTO createDTO() {
        return new FeatureDTO();
    }
}
