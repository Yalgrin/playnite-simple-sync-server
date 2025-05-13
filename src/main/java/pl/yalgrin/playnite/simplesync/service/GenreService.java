package pl.yalgrin.playnite.simplesync.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import pl.yalgrin.playnite.simplesync.domain.Genre;
import pl.yalgrin.playnite.simplesync.dto.GenreDTO;
import pl.yalgrin.playnite.simplesync.enums.ObjectType;
import pl.yalgrin.playnite.simplesync.mapper.GenreMapper;
import pl.yalgrin.playnite.simplesync.repository.GenreRepository;

@Service
public class GenreService extends AbstractObjectService<Genre, GenreDTO> {
    public GenreService(GenreRepository repository, GenreMapper mapper, ChangeService changeService,
                        ChangeListenerService changeListenerService, TransactionalOperator transactionalOperator) {
        super(repository, mapper, changeService, changeListenerService, transactionalOperator);
    }

    @Override
    protected Genre createEntityFromDTO(GenreDTO dto) {
        return Genre.builder().playniteId(dto.getId()).build();
    }

    @Override
    protected ObjectType getObjectType() {
        return ObjectType.Genre;
    }
}
