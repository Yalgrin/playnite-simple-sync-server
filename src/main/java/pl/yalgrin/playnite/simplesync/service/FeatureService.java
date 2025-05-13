package pl.yalgrin.playnite.simplesync.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import pl.yalgrin.playnite.simplesync.domain.Feature;
import pl.yalgrin.playnite.simplesync.dto.FeatureDTO;
import pl.yalgrin.playnite.simplesync.enums.ObjectType;
import pl.yalgrin.playnite.simplesync.mapper.FeatureMapper;
import pl.yalgrin.playnite.simplesync.repository.FeatureRepository;

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
