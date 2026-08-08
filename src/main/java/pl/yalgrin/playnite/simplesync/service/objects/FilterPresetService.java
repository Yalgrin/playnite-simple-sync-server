package pl.yalgrin.playnite.simplesync.service.objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import pl.yalgrin.playnite.simplesync.change.service.ChangeListenerService;
import pl.yalgrin.playnite.simplesync.change.service.ChangeService;
import pl.yalgrin.playnite.simplesync.common.enums.ObjectType;
import pl.yalgrin.playnite.simplesync.library.domain.FilterPreset;
import pl.yalgrin.playnite.simplesync.library.dto.FilterPresetDTO;
import pl.yalgrin.playnite.simplesync.library.repository.FilterPresetRepository;
import pl.yalgrin.playnite.simplesync.mapper.objects.FilterPresetMapper;

@Service
public class FilterPresetService extends AbstractObjectService<FilterPreset, FilterPresetDTO> {
    public FilterPresetService(FilterPresetRepository repository, FilterPresetMapper mapper,
                               ChangeService changeService, ChangeListenerService changeListenerService,
                               TransactionalOperator transactionalOperator) {
        super(repository, mapper, changeService, changeListenerService, transactionalOperator);
    }

    @Override
    protected FilterPreset createEntityFromDTO(FilterPresetDTO dto) {
        FilterPreset filterPreset = new FilterPreset();
        filterPreset.setPlayniteId(dto.getId());
        return filterPreset;
    }

    @Override
    protected ObjectType getObjectType() {
        return ObjectType.FILTER_PRESET;
    }
}
