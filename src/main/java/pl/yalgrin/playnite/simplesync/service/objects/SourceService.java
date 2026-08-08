package pl.yalgrin.playnite.simplesync.service.objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import pl.yalgrin.playnite.simplesync.change.service.ChangeListenerService;
import pl.yalgrin.playnite.simplesync.change.service.ChangeService;
import pl.yalgrin.playnite.simplesync.common.enums.ObjectType;
import pl.yalgrin.playnite.simplesync.library.domain.Source;
import pl.yalgrin.playnite.simplesync.library.dto.SourceDTO;
import pl.yalgrin.playnite.simplesync.library.repository.SourceRepository;
import pl.yalgrin.playnite.simplesync.mapper.objects.SourceMapper;

@Service
public class SourceService extends AbstractObjectService<Source, SourceDTO> {
    public SourceService(SourceRepository repository, SourceMapper mapper, ChangeService changeService,
                         ChangeListenerService changeListenerService, TransactionalOperator transactionalOperator) {
        super(repository, mapper, changeService, changeListenerService, transactionalOperator);
    }

    @Override
    protected Source createEntityFromDTO(SourceDTO dto) {
        Source source = new Source();
        source.setPlayniteId(dto.getId());
        return source;
    }

    @Override
    protected ObjectType getObjectType() {
        return ObjectType.SOURCE;
    }
}
