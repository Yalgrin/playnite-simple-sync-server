package pl.yalgrin.playnite.simplesync.mapper.objects;

import org.springframework.stereotype.Component;
import pl.yalgrin.playnite.simplesync.dto.objects.CompanyDTO;
import pl.yalgrin.playnite.simplesync.library.domain.Company;

@Component
public class CompanyMapper extends AbstractObjectMapper<Company, CompanyDTO> {

    @Override
    protected CompanyDTO createDTO() {
        return new CompanyDTO();
    }
}
