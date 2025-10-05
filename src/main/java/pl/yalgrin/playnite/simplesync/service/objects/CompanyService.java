package pl.yalgrin.playnite.simplesync.service.objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import pl.yalgrin.playnite.simplesync.domain.objects.Company;
import pl.yalgrin.playnite.simplesync.dto.objects.CompanyDTO;
import pl.yalgrin.playnite.simplesync.enums.ObjectType;
import pl.yalgrin.playnite.simplesync.mapper.objects.CompanyMapper;
import pl.yalgrin.playnite.simplesync.repository.objects.CompanyRepository;
import pl.yalgrin.playnite.simplesync.service.ChangeListenerService;
import pl.yalgrin.playnite.simplesync.service.ChangeService;

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
