package pl.yalgrin.playnite.simplesync.mapper.objects;

import org.springframework.stereotype.Component;
import pl.yalgrin.playnite.simplesync.dto.objects.CategoryDTO;
import pl.yalgrin.playnite.simplesync.library.domain.Category;

@Component
public class CategoryMapper extends AbstractObjectMapper<Category, CategoryDTO> {

    @Override
    protected CategoryDTO createDTO() {
        return new CategoryDTO();
    }
}
