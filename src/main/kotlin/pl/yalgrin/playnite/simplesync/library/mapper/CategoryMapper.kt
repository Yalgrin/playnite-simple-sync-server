package pl.yalgrin.playnite.simplesync.library.mapper

import org.springframework.stereotype.Component
import pl.yalgrin.playnite.simplesync.library.domain.Category
import pl.yalgrin.playnite.simplesync.library.dto.CategoryDTO

@Component
class CategoryMapper : LibraryObjectMapperImpl<Category, CategoryDTO>() {
    override fun createDTO(): CategoryDTO = CategoryDTO()
}