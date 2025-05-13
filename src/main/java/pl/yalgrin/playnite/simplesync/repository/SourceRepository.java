package pl.yalgrin.playnite.simplesync.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.stereotype.Repository;
import pl.yalgrin.playnite.simplesync.domain.Source;
import reactor.core.publisher.Flux;

@Repository
public interface SourceRepository extends ObjectRepository<Source> {
    @Query("select c.id from playnite_source c order by c.id asc")
    Flux<Long> findAllIds();
}
