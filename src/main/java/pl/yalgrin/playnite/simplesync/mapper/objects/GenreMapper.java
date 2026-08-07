package pl.yalgrin.playnite.simplesync.mapper.objects;

import org.springframework.stereotype.Component;
import pl.yalgrin.playnite.simplesync.dto.objects.GenreDTO;
import pl.yalgrin.playnite.simplesync.library.domain.Genre;

@Component
public class GenreMapper extends AbstractObjectMapper<Genre, GenreDTO> {

    @Override
    protected GenreDTO createDTO() {
        return new GenreDTO();
    }
}
