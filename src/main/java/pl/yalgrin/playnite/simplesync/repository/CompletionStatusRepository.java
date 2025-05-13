package pl.yalgrin.playnite.simplesync.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.stereotype.Repository;
import pl.yalgrin.playnite.simplesync.domain.CompletionStatus;
import reactor.core.publisher.Flux;

@Repository
public interface CompletionStatusRepository extends ObjectRepository<CompletionStatus> {
    @Query("select c.id from playnite_comp_status c order by c.id asc")
    Flux<Long> findAllIds();
}
