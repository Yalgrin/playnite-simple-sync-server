package pl.yalgrin.playnite.simplesync.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import pl.yalgrin.playnite.simplesync.domain.Region;
import pl.yalgrin.playnite.simplesync.dto.RegionDTO;
import pl.yalgrin.playnite.simplesync.enums.ObjectType;
import pl.yalgrin.playnite.simplesync.mapper.RegionMapper;
import pl.yalgrin.playnite.simplesync.repository.RegionRepository;

@Service
public class RegionService extends AbstractObjectService<Region, RegionDTO> {
    public RegionService(RegionRepository repository, RegionMapper mapper, ChangeService changeService,
                         ChangeListenerService changeListenerService, TransactionalOperator transactionalOperator) {
        super(repository, mapper, changeService, changeListenerService, transactionalOperator);
    }

    @Override
    protected Region createEntityFromDTO(RegionDTO dto) {
        return Region.builder().playniteId(dto.getId()).build();
    }

    @Override
    protected ObjectType getObjectType() {
        return ObjectType.Region;
    }
}
