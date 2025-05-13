package pl.yalgrin.playnite.simplesync.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import pl.yalgrin.playnite.simplesync.domain.Change;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ChangeRepository extends R2dbcRepository<Change, Long> {
    @Query("select c.* from playnite_change c where c.id > :lastId order by c.id asc")
    Flux<Change> findFromLastId(Long lastId);

    @Query("select max(c.id) from playnite_change c")
    Mono<Long> findMaxId();
}
