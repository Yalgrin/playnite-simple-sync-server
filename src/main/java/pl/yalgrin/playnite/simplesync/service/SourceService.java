package pl.yalgrin.playnite.simplesync.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import pl.yalgrin.playnite.simplesync.domain.Source;
import pl.yalgrin.playnite.simplesync.dto.SourceDTO;
import pl.yalgrin.playnite.simplesync.enums.ObjectType;
import pl.yalgrin.playnite.simplesync.mapper.SourceMapper;
import pl.yalgrin.playnite.simplesync.repository.SourceRepository;

@Service
public class SourceService extends AbstractObjectService<Source, SourceDTO> {
    public SourceService(SourceRepository repository, SourceMapper mapper, ChangeService changeService,
                         ChangeListenerService changeListenerService, TransactionalOperator transactionalOperator) {
        super(repository, mapper, changeService, changeListenerService, transactionalOperator);
    }

    @Override
    protected Source createEntityFromDTO(SourceDTO dto) {
        return Source.builder().playniteId(dto.getId()).build();
    }

    @Override
    protected ObjectType getObjectType() {
        return ObjectType.Source;
    }
}
