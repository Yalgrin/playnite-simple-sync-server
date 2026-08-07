package pl.yalgrin.playnite.simplesync.service.objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import pl.yalgrin.playnite.simplesync.change.service.ChangeListenerService;
import pl.yalgrin.playnite.simplesync.dto.objects.AgeRatingDTO;
import pl.yalgrin.playnite.simplesync.enums.ObjectType;
import pl.yalgrin.playnite.simplesync.library.domain.AgeRating;
import pl.yalgrin.playnite.simplesync.library.repository.AgeRatingRepository;
import pl.yalgrin.playnite.simplesync.mapper.objects.AgeRatingMapper;
import pl.yalgrin.playnite.simplesync.service.ChangeService;

@Service
public class AgeRatingService extends AbstractObjectService<AgeRating, AgeRatingDTO> {
    public AgeRatingService(AgeRatingRepository repository, AgeRatingMapper mapper, ChangeService changeService,
                            ChangeListenerService changeListenerService,
                            TransactionalOperator transactionalOperator) {
        super(repository, mapper, changeService, changeListenerService, transactionalOperator);
    }

    @Override
    protected AgeRating createEntityFromDTO(AgeRatingDTO dto) {
        AgeRating ageRating = new AgeRating();
        ageRating.setPlayniteId(dto.getId());
        return ageRating;
    }

    @Override
    protected ObjectType getObjectType() {
        return ObjectType.AgeRating;
    }
}
