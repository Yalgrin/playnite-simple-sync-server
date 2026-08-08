package pl.yalgrin.playnite.simplesync.service.objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import pl.yalgrin.playnite.simplesync.change.service.ChangeListenerService;
import pl.yalgrin.playnite.simplesync.change.service.ChangeService;
import pl.yalgrin.playnite.simplesync.common.enums.ObjectType;
import pl.yalgrin.playnite.simplesync.library.domain.Company;
import pl.yalgrin.playnite.simplesync.library.dto.CompanyDTO;
import pl.yalgrin.playnite.simplesync.library.repository.CompanyRepository;
import pl.yalgrin.playnite.simplesync.mapper.objects.CompanyMapper;

@Service
public class CompanyService extends AbstractObjectService<Company, CompanyDTO> {
    public CompanyService(CompanyRepository repository, CompanyMapper mapper, ChangeService changeService,
                          ChangeListenerService changeListenerService,
                          TransactionalOperator transactionalOperator) {
        super(repository, mapper, changeService, changeListenerService, transactionalOperator);
    }

    @Override
    protected Company createEntityFromDTO(CompanyDTO dto) {
        Company company = new Company();
        company.setPlayniteId(dto.getId());
        return company;
    }

    @Override
    protected ObjectType getObjectType() {
        return ObjectType.COMPANY;
    }
}
