package pl.yalgrin.playnite.simplesync.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import pl.yalgrin.playnite.simplesync.domain.CompletionStatus;
import pl.yalgrin.playnite.simplesync.dto.CompletionStatusDTO;
import pl.yalgrin.playnite.simplesync.enums.ObjectType;
import pl.yalgrin.playnite.simplesync.mapper.CompletionStatusMapper;
import pl.yalgrin.playnite.simplesync.repository.CompletionStatusRepository;

@Service
public class CompletionStatusService extends AbstractObjectService<CompletionStatus, CompletionStatusDTO> {
    public CompletionStatusService(CompletionStatusRepository repository, CompletionStatusMapper mapper,
                                   ChangeService changeService, ChangeListenerService changeListenerService,
                                   TransactionalOperator transactionalOperator) {
        super(repository, mapper, changeService, changeListenerService, transactionalOperator);
    }

    @Override
    protected CompletionStatus createEntityFromDTO(CompletionStatusDTO dto) {
        return CompletionStatus.builder().playniteId(dto.getId()).build();
    }

    @Override
    protected ObjectType getObjectType() {
        return ObjectType.CompletionStatus;
    }
}
