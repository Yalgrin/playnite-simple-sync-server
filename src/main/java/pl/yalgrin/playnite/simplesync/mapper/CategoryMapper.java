package pl.yalgrin.playnite.simplesync.mapper;

import org.springframework.stereotype.Component;
import pl.yalgrin.playnite.simplesync.domain.Category;
import pl.yalgrin.playnite.simplesync.dto.CategoryDTO;

@Component
public class CategoryMapper extends AbstractObjectMapper<Category, CategoryDTO> {

    @Override
    protected CategoryDTO createDTO() {
        return new CategoryDTO();
    }
}
