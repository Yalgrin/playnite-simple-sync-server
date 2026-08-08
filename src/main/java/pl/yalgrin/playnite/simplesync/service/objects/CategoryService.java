package pl.yalgrin.playnite.simplesync.service.objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import pl.yalgrin.playnite.simplesync.change.service.ChangeListenerService;
import pl.yalgrin.playnite.simplesync.change.service.ChangeService;
import pl.yalgrin.playnite.simplesync.common.enums.ObjectType;
import pl.yalgrin.playnite.simplesync.library.domain.Category;
import pl.yalgrin.playnite.simplesync.library.dto.CategoryDTO;
import pl.yalgrin.playnite.simplesync.library.repository.CategoryRepository;
import pl.yalgrin.playnite.simplesync.mapper.objects.CategoryMapper;

@Service
public class CategoryService extends AbstractObjectService<Category, CategoryDTO> {
    public CategoryService(CategoryRepository repository, CategoryMapper mapper, ChangeService changeService,
                           ChangeListenerService changeListenerService, TransactionalOperator transactionalOperator) {
        super(repository, mapper, changeService, changeListenerService, transactionalOperator);
    }

    @Override
    protected Category createEntityFromDTO(CategoryDTO dto) {
        Category category = new Category();
        category.setPlayniteId(dto.getId());
        return category;
    }

    @Override
    protected ObjectType getObjectType() {
        return ObjectType.CATEGORY;
    }
}
