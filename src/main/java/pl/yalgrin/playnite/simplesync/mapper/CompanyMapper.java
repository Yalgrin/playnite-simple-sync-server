package pl.yalgrin.playnite.simplesync.mapper;

import org.springframework.stereotype.Component;
import pl.yalgrin.playnite.simplesync.domain.Company;
import pl.yalgrin.playnite.simplesync.dto.CompanyDTO;

@Component
public class CompanyMapper extends AbstractObjectMapper<Company, CompanyDTO> {

    @Override
    protected CompanyDTO createDTO() {
        return new CompanyDTO();
    }
}
