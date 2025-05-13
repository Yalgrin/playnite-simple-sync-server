package pl.yalgrin.playnite.simplesync.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.NoRepositoryBean;
import pl.yalgrin.playnite.simplesync.domain.AbstractObjectEntity;
import reactor.core.publisher.Flux;

import java.util.Collection;

@NoRepositoryBean
public interface ObjectRepository<E extends AbstractObjectEntity> extends R2dbcRepository<E, Long> {
    Flux<E> findByPlayniteId(String playniteId);

    Flux<E> findByPlayniteIdIn(Collection<String> playniteId);

    Flux<Long> findIdsByPlayniteIdIn(Collection<String> playniteId);

    Flux<E> findByName(String name);

    Flux<E> findByPlayniteIdAndNameAndRemovedIsFalse(String playniteId, String name);
}
