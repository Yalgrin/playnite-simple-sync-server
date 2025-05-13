package pl.yalgrin.playnite.simplesync.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.stereotype.Repository;
import pl.yalgrin.playnite.simplesync.domain.Tag;
import reactor.core.publisher.Flux;

@Repository
public interface TagRepository extends ObjectRepository<Tag> {
    @Query("select c.id from playnite_tag c order by c.id asc")
    Flux<Long> findAllIds();
}
