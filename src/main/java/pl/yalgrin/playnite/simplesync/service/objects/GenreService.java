package pl.yalgrin.playnite.simplesync.service.objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import pl.yalgrin.playnite.simplesync.change.service.ChangeListenerService;
import pl.yalgrin.playnite.simplesync.dto.objects.GenreDTO;
import pl.yalgrin.playnite.simplesync.enums.ObjectType;
import pl.yalgrin.playnite.simplesync.library.domain.Genre;
import pl.yalgrin.playnite.simplesync.library.repository.GenreRepository;
import pl.yalgrin.playnite.simplesync.mapper.objects.GenreMapper;
import pl.yalgrin.playnite.simplesync.service.ChangeService;

@Service
public class GenreService extends AbstractObjectService<Genre, GenreDTO> {
    public GenreService(GenreRepository repository, GenreMapper mapper, ChangeService changeService,
                        ChangeListenerService changeListenerService, TransactionalOperator transactionalOperator) {
        super(repository, mapper, changeService, changeListenerService, transactionalOperator);
    }

    @Override
    protected Genre createEntityFromDTO(GenreDTO dto) {
        Genre genre = new Genre();
        genre.setPlayniteId(dto.getId());
        return genre;
    }

    @Override
    protected ObjectType getObjectType() {
        return ObjectType.Genre;
    }
}
