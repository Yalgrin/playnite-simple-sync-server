package pl.yalgrin.playnite.simplesync.mapper;

import org.springframework.stereotype.Component;
import pl.yalgrin.playnite.simplesync.domain.AgeRating;
import pl.yalgrin.playnite.simplesync.dto.AgeRatingDTO;

@Component
public class AgeRatingMapper extends AbstractObjectMapper<AgeRating, AgeRatingDTO> {

    @Override
    protected AgeRatingDTO createDTO() {
        return new AgeRatingDTO();
    }
}
