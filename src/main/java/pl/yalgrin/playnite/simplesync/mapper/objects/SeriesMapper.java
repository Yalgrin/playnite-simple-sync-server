package pl.yalgrin.playnite.simplesync.mapper.objects;

import org.springframework.stereotype.Component;
import pl.yalgrin.playnite.simplesync.domain.objects.Series;
import pl.yalgrin.playnite.simplesync.dto.objects.SeriesDTO;

@Component
public class SeriesMapper extends AbstractObjectMapper<Series, SeriesDTO> {

    @Override
    protected SeriesDTO createDTO() {
        return new SeriesDTO();
    }
}
