package pl.yalgrin.playnite.simplesync.mapper;

import org.springframework.stereotype.Component;
import pl.yalgrin.playnite.simplesync.domain.Genre;
import pl.yalgrin.playnite.simplesync.dto.GenreDTO;

@Component
public class GenreMapper extends AbstractObjectMapper<Genre, GenreDTO> {

    @Override
    protected GenreDTO createDTO() {
        return new GenreDTO();
    }
}
