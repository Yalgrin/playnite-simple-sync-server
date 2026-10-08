package pl.yalgrin.playnite.simplesync.panel.mapper

import org.springframework.stereotype.Component
import pl.yalgrin.playnite.simplesync.library.domain.Company
import pl.yalgrin.playnite.simplesync.panel.dto.PanelCompanyDTO

@Component
class PanelCompanyMapper : PanelObjectMapperImpl<Company, PanelCompanyDTO>() {
    override fun createDTO(): PanelCompanyDTO = PanelCompanyDTO()
}
