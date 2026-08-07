package pl.yalgrin.playnite.simplesync.service.objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import pl.yalgrin.playnite.simplesync.change.service.ChangeListenerService;
import pl.yalgrin.playnite.simplesync.dto.objects.FeatureDTO;
import pl.yalgrin.playnite.simplesync.enums.ObjectType;
import pl.yalgrin.playnite.simplesync.library.domain.Feature;
import pl.yalgrin.playnite.simplesync.library.repository.FeatureRepository;
import pl.yalgrin.playnite.simplesync.mapper.objects.FeatureMapper;
import pl.yalgrin.playnite.simplesync.service.ChangeService;

@Service
public class FeatureService extends AbstractObjectService<Feature, FeatureDTO> {
    public FeatureService(FeatureRepository repository, FeatureMapper mapper, ChangeService changeService,
                          ChangeListenerService changeListenerService, TransactionalOperator transactionalOperator) {
        super(repository, mapper, changeService, changeListenerService, transactionalOperator);
    }

    @Override
    protected Feature createEntityFromDTO(FeatureDTO dto) {
        Feature feature = new Feature();
        feature.setPlayniteId(dto.getId());
        return feature;
    }

    @Override
    protected ObjectType getObjectType() {
        return ObjectType.Feature;
    }
}
