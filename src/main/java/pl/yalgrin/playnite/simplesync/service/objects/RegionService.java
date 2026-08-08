package pl.yalgrin.playnite.simplesync.service.objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import pl.yalgrin.playnite.simplesync.change.service.ChangeListenerService;
import pl.yalgrin.playnite.simplesync.change.service.ChangeService;
import pl.yalgrin.playnite.simplesync.common.enums.ObjectType;
import pl.yalgrin.playnite.simplesync.library.domain.Region;
import pl.yalgrin.playnite.simplesync.library.dto.RegionDTO;
import pl.yalgrin.playnite.simplesync.library.repository.RegionRepository;
import pl.yalgrin.playnite.simplesync.mapper.objects.RegionMapper;

@Service
public class RegionService extends AbstractObjectService<Region, RegionDTO> {
    public RegionService(RegionRepository repository, RegionMapper mapper, ChangeService changeService,
                         ChangeListenerService changeListenerService, TransactionalOperator transactionalOperator) {
        super(repository, mapper, changeService, changeListenerService, transactionalOperator);
    }

    @Override
    protected Region createEntityFromDTO(RegionDTO dto) {
        Region region = new Region();
        region.setPlayniteId(dto.getId());
        return region;
    }

    @Override
    protected ObjectType getObjectType() {
        return ObjectType.REGION;
    }
}
