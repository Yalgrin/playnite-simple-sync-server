package pl.yalgrin.playnite.simplesync.service.objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import pl.yalgrin.playnite.simplesync.domain.objects.Feature;
import pl.yalgrin.playnite.simplesync.dto.objects.FeatureDTO;
import pl.yalgrin.playnite.simplesync.enums.ObjectType;
import pl.yalgrin.playnite.simplesync.mapper.objects.FeatureMapper;
import pl.yalgrin.playnite.simplesync.repository.objects.FeatureRepository;
import pl.yalgrin.playnite.simplesync.service.ChangeListenerService;
import pl.yalgrin.playnite.simplesync.service.ChangeService;

@Service
public class FeatureService extends AbstractObjectService<Feature, FeatureDTO> {
    public FeatureService(FeatureRepository repository, FeatureMapper mapper, ChangeService changeService,
                          ChangeListenerService changeListenerService, TransactionalOperator transactionalOperator) {
        super(repository, mapper, changeService, changeListenerService, transactionalOperator);
    }

    @Override
    protected Feature createEntityFromDTO(FeatureDTO dto) {
        return Feature.builder().playniteId(dto.getId()).build();
    }

    @Override
    protected ObjectType getObjectType() {
        return ObjectType.Feature;
    }
}
