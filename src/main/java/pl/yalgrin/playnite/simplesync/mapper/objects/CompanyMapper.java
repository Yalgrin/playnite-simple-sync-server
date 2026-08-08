package pl.yalgrin.playnite.simplesync.mapper.objects;

import org.springframework.stereotype.Component;
import pl.yalgrin.playnite.simplesync.library.domain.Company;
import pl.yalgrin.playnite.simplesync.library.dto.CompanyDTO;

@Component
public class CompanyMapper extends AbstractObjectMapper<Company, CompanyDTO> {

    @Override
    protected CompanyDTO createDTO() {
        return new CompanyDTO();
    }
}
