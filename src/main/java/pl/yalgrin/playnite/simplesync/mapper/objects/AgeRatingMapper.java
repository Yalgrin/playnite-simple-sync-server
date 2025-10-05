package pl.yalgrin.playnite.simplesync.mapper.objects;

import org.springframework.stereotype.Component;
import pl.yalgrin.playnite.simplesync.domain.objects.AgeRating;
import pl.yalgrin.playnite.simplesync.dto.objects.AgeRatingDTO;

@Component
public class AgeRatingMapper extends AbstractObjectMapper<AgeRating, AgeRatingDTO> {

    @Override
    protected AgeRatingDTO createDTO() {
        return new AgeRatingDTO();
    }
}
