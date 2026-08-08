package pl.yalgrin.playnite.simplesync.mapper.objects;

import org.springframework.stereotype.Component;
import pl.yalgrin.playnite.simplesync.library.domain.Genre;
import pl.yalgrin.playnite.simplesync.library.dto.GenreDTO;

@Component
public class GenreMapper extends AbstractObjectMapper<Genre, GenreDTO> {

    @Override
    protected GenreDTO createDTO() {
        return new GenreDTO();
    }
}
