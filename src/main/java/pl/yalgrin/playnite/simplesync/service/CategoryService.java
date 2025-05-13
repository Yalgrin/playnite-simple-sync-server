package pl.yalgrin.playnite.simplesync.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import pl.yalgrin.playnite.simplesync.domain.Category;
import pl.yalgrin.playnite.simplesync.dto.CategoryDTO;
import pl.yalgrin.playnite.simplesync.enums.ObjectType;
import pl.yalgrin.playnite.simplesync.mapper.CategoryMapper;
import pl.yalgrin.playnite.simplesync.repository.CategoryRepository;

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
