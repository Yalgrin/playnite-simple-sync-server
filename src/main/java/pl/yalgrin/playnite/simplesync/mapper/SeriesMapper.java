package pl.yalgrin.playnite.simplesync.mapper;

import org.springframework.stereotype.Component;
import pl.yalgrin.playnite.simplesync.domain.Series;
import pl.yalgrin.playnite.simplesync.dto.SeriesDTO;

@Component
public class SeriesMapper extends AbstractObjectMapper<Series, SeriesDTO> {

    @Override
    protected SeriesDTO createDTO() {
        return new SeriesDTO();
    }
}
