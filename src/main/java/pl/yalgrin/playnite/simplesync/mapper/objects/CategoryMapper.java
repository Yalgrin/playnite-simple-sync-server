package pl.yalgrin.playnite.simplesync.mapper.objects;

import org.springframework.stereotype.Component;
import pl.yalgrin.playnite.simplesync.library.domain.Category;
import pl.yalgrin.playnite.simplesync.library.dto.CategoryDTO;

@Component
public class CategoryMapper extends AbstractObjectMapper<Category, CategoryDTO> {

    @Override
    protected CategoryDTO createDTO() {
        return new CategoryDTO();
    }
}
