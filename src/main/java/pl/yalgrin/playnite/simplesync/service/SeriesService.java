package pl.yalgrin.playnite.simplesync.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import pl.yalgrin.playnite.simplesync.domain.Series;
import pl.yalgrin.playnite.simplesync.dto.SeriesDTO;
import pl.yalgrin.playnite.simplesync.enums.ObjectType;
import pl.yalgrin.playnite.simplesync.mapper.SeriesMapper;
import pl.yalgrin.playnite.simplesync.repository.SeriesRepository;

@Service
public class SeriesService extends AbstractObjectService<Series, SeriesDTO> {
    public SeriesService(SeriesRepository repository, SeriesMapper mapper, ChangeService changeService,
                         ChangeListenerService changeListenerService, TransactionalOperator transactionalOperator) {
        super(repository, mapper, changeService, changeListenerService, transactionalOperator);
    }

    @Override
    protected Series createEntityFromDTO(SeriesDTO dto) {
        return Series.builder().playniteId(dto.getId()).build();
    }

    @Override
    protected ObjectType getObjectType() {
        return ObjectType.Series;
    }
}
