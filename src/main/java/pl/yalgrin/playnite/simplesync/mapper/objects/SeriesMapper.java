package pl.yalgrin.playnite.simplesync.mapper.objects;

import org.springframework.stereotype.Component;
import pl.yalgrin.playnite.simplesync.library.domain.Series;
import pl.yalgrin.playnite.simplesync.library.dto.SeriesDTO;

@Component
public class SeriesMapper extends AbstractObjectMapper<Series, SeriesDTO> {

    @Override
    protected SeriesDTO createDTO() {
        return new SeriesDTO();
    }
}
