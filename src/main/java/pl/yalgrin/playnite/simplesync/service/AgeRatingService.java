package pl.yalgrin.playnite.simplesync.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import pl.yalgrin.playnite.simplesync.domain.AgeRating;
import pl.yalgrin.playnite.simplesync.dto.AgeRatingDTO;
import pl.yalgrin.playnite.simplesync.enums.ObjectType;
import pl.yalgrin.playnite.simplesync.mapper.AgeRatingMapper;
import pl.yalgrin.playnite.simplesync.repository.AgeRatingRepository;

@Service
public class AgeRatingService extends AbstractObjectService<AgeRating, AgeRatingDTO> {
    public AgeRatingService(AgeRatingRepository repository, AgeRatingMapper mapper, ChangeService changeService,
                            ChangeListenerService changeListenerService,
                            TransactionalOperator transactionalOperator) {
        super(repository, mapper, changeService, changeListenerService, transactionalOperator);
    }

    @Override
    protected AgeRating createEntityFromDTO(AgeRatingDTO dto) {
        return AgeRating.builder().playniteId(dto.getId()).build();
    }

    @Override
    protected ObjectType getObjectType() {
        return ObjectType.AgeRating;
    }
}
