package pl.yalgrin.playnite.simplesync.panel.mapper

import org.springframework.stereotype.Component
import pl.yalgrin.playnite.simplesync.library.domain.Category
import pl.yalgrin.playnite.simplesync.panel.dto.PanelCategoryDTO

@Component
class PanelCategoryMapper : PanelObjectMapperImpl<Category, PanelCategoryDTO>() {
    override fun createDTO(): PanelCategoryDTO = PanelCategoryDTO()
}
