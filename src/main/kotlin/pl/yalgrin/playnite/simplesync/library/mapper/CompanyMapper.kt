package pl.yalgrin.playnite.simplesync.library.mapper

import org.springframework.stereotype.Component
import pl.yalgrin.playnite.simplesync.library.domain.Company
import pl.yalgrin.playnite.simplesync.library.dto.CompanyDTO

@Component
class CompanyMapper : LibraryObjectMapperImpl<Company, CompanyDTO>() {
    override fun createDTO(): CompanyDTO = CompanyDTO()
}