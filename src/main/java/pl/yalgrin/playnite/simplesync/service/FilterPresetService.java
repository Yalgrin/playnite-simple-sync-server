package pl.yalgrin.playnite.simplesync.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import pl.yalgrin.playnite.simplesync.domain.FilterPreset;
import pl.yalgrin.playnite.simplesync.dto.FilterPresetDTO;
import pl.yalgrin.playnite.simplesync.enums.ObjectType;
import pl.yalgrin.playnite.simplesync.mapper.FilterPresetMapper;
import pl.yalgrin.playnite.simplesync.repository.FilterPresetRepository;

@Service
public class FilterPresetService extends AbstractObjectService<FilterPreset, FilterPresetDTO> {
    public FilterPresetService(FilterPresetRepository repository, FilterPresetMapper mapper,
                               ChangeService changeService, ChangeListenerService changeListenerService,
                               TransactionalOperator transactionalOperator) {
        super(repository, mapper, changeService, changeListenerService, transactionalOperator);
    }

    @Override
    protected FilterPreset createEntityFromDTO(FilterPresetDTO dto) {
        return FilterPreset.builder().playniteId(dto.getId()).build();
    }

    @Override
    protected ObjectType getObjectType() {
        return ObjectType.FilterPreset;
    }
}
