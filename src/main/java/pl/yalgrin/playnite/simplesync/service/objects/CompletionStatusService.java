package pl.yalgrin.playnite.simplesync.service.objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import pl.yalgrin.playnite.simplesync.change.service.ChangeListenerService;
import pl.yalgrin.playnite.simplesync.dto.objects.CompletionStatusDTO;
import pl.yalgrin.playnite.simplesync.enums.ObjectType;
import pl.yalgrin.playnite.simplesync.library.domain.CompletionStatus;
import pl.yalgrin.playnite.simplesync.library.repository.CompletionStatusRepository;
import pl.yalgrin.playnite.simplesync.mapper.objects.CompletionStatusMapper;
import pl.yalgrin.playnite.simplesync.service.ChangeService;

@Service
public class CompletionStatusService extends AbstractObjectService<CompletionStatus, CompletionStatusDTO> {
    public CompletionStatusService(CompletionStatusRepository repository, CompletionStatusMapper mapper,
                                   ChangeService changeService, ChangeListenerService changeListenerService,
                                   TransactionalOperator transactionalOperator) {
        super(repository, mapper, changeService, changeListenerService, transactionalOperator);
    }

    @Override
    protected CompletionStatus createEntityFromDTO(CompletionStatusDTO dto) {
        CompletionStatus completionStatus = new CompletionStatus();
        completionStatus.setPlayniteId(dto.getId());
        return completionStatus;
    }

    @Override
    protected ObjectType getObjectType() {
        return ObjectType.CompletionStatus;
    }
}
