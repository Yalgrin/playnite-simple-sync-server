package pl.yalgrin.playnite.simplesync.service.objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import pl.yalgrin.playnite.simplesync.domain.objects.Category;
import pl.yalgrin.playnite.simplesync.dto.objects.CategoryDTO;
import pl.yalgrin.playnite.simplesync.enums.ObjectType;
import pl.yalgrin.playnite.simplesync.mapper.objects.CategoryMapper;
import pl.yalgrin.playnite.simplesync.repository.objects.CategoryRepository;
import pl.yalgrin.playnite.simplesync.service.ChangeListenerService;
import pl.yalgrin.playnite.simplesync.service.ChangeService;

@Service
public class CategoryService extends AbstractObjectService<Category, CategoryDTO> {
    public CategoryService(CategoryRepository repository, CategoryMapper mapper, ChangeService changeService,
                           ChangeListenerService changeListenerService, TransactionalOperator transactionalOperator) {
        super(repository, mapper, changeService, changeListenerService, transactionalOperator);
    }

    @Override
    protected Category createEntityFromDTO(CategoryDTO dto) {
        return Category.builder().playniteId(dto.getId()).build();
    }

    @Override
    protected ObjectType getObjectType() {
        return ObjectType.Category;
    }
}
