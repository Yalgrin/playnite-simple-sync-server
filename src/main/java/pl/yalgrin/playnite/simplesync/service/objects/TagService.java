package pl.yalgrin.playnite.simplesync.service.objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import pl.yalgrin.playnite.simplesync.domain.objects.Tag;
import pl.yalgrin.playnite.simplesync.dto.objects.TagDTO;
import pl.yalgrin.playnite.simplesync.enums.ObjectType;
import pl.yalgrin.playnite.simplesync.mapper.objects.TagMapper;
import pl.yalgrin.playnite.simplesync.repository.objects.TagRepository;
import pl.yalgrin.playnite.simplesync.service.ChangeListenerService;
import pl.yalgrin.playnite.simplesync.service.ChangeService;

@Service
public class TagService extends AbstractObjectService<Tag, TagDTO> {
    public TagService(TagRepository repository, TagMapper mapper, ChangeService changeService,
                      ChangeListenerService changeListenerService, TransactionalOperator transactionalOperator) {
        super(repository, mapper, changeService, changeListenerService, transactionalOperator);
    }

    @Override
    protected Tag createEntityFromDTO(TagDTO dto) {
        return Tag.builder().playniteId(dto.getId()).build();
    }

    @Override
    protected ObjectType getObjectType() {
        return ObjectType.Tag;
    }
}
