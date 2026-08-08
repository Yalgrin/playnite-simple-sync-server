package pl.yalgrin.playnite.simplesync.service.objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import pl.yalgrin.playnite.simplesync.change.service.ChangeListenerService;
import pl.yalgrin.playnite.simplesync.change.service.ChangeService;
import pl.yalgrin.playnite.simplesync.common.enums.ObjectType;
import pl.yalgrin.playnite.simplesync.library.domain.Series;
import pl.yalgrin.playnite.simplesync.library.dto.SeriesDTO;
import pl.yalgrin.playnite.simplesync.library.repository.SeriesRepository;
import pl.yalgrin.playnite.simplesync.mapper.objects.SeriesMapper;

@Service
public class SeriesService extends AbstractObjectService<Series, SeriesDTO> {
    public SeriesService(SeriesRepository repository, SeriesMapper mapper, ChangeService changeService,
                         ChangeListenerService changeListenerService, TransactionalOperator transactionalOperator) {
        super(repository, mapper, changeService, changeListenerService, transactionalOperator);
    }

    @Override
    protected Series createEntityFromDTO(SeriesDTO dto) {
        Series series = new Series();
        series.setPlayniteId(dto.getId());
        return series;
    }

    @Override
    protected ObjectType getObjectType() {
        return ObjectType.SERIES;
    }
}
