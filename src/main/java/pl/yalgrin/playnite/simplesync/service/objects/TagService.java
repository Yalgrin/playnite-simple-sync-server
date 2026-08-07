package pl.yalgrin.playnite.simplesync.service.objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import pl.yalgrin.playnite.simplesync.change.service.ChangeListenerService;
import pl.yalgrin.playnite.simplesync.dto.objects.TagDTO;
import pl.yalgrin.playnite.simplesync.enums.ObjectType;
import pl.yalgrin.playnite.simplesync.library.domain.Tag;
import pl.yalgrin.playnite.simplesync.library.repository.TagRepository;
import pl.yalgrin.playnite.simplesync.mapper.objects.TagMapper;
import pl.yalgrin.playnite.simplesync.service.ChangeService;

@Service
public class TagService extends AbstractObjectService<Tag, TagDTO> {
    public TagService(TagRepository repository, TagMapper mapper, ChangeService changeService,
                      ChangeListenerService changeListenerService, TransactionalOperator transactionalOperator) {
        super(repository, mapper, changeService, changeListenerService, transactionalOperator);
    }

    @Override
    protected Tag createEntityFromDTO(TagDTO dto) {
        Tag tag = new Tag();
        tag.setPlayniteId(dto.getId());
        return tag;
    }

    @Override
    protected ObjectType getObjectType() {
        return ObjectType.Tag;
    }
}
