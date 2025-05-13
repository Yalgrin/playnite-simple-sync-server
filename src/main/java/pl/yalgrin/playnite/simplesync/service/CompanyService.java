package pl.yalgrin.playnite.simplesync.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import pl.yalgrin.playnite.simplesync.domain.Company;
import pl.yalgrin.playnite.simplesync.dto.CompanyDTO;
import pl.yalgrin.playnite.simplesync.enums.ObjectType;
import pl.yalgrin.playnite.simplesync.mapper.CompanyMapper;
import pl.yalgrin.playnite.simplesync.repository.CompanyRepository;

@Service
public class CompanyService extends AbstractObjectService<Company, CompanyDTO> {
    public CompanyService(CompanyRepository repository, CompanyMapper mapper, ChangeService changeService,
                          ChangeListenerService changeListenerService,
                          TransactionalOperator transactionalOperator) {
        super(repository, mapper, changeService, changeListenerService, transactionalOperator);
    }

    @Override
    protected Company createEntityFromDTO(CompanyDTO dto) {
        return Company.builder().playniteId(dto.getId()).build();
    }

    @Override
    protected ObjectType getObjectType() {
        return ObjectType.Company;
    }
}
