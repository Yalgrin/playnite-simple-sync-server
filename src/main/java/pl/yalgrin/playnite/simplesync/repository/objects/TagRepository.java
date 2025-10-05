package pl.yalgrin.playnite.simplesync.repository.objects;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.stereotype.Repository;
import pl.yalgrin.playnite.simplesync.domain.objects.Tag;
import reactor.core.publisher.Flux;

@Repository
public interface TagRepository extends ObjectRepository<Tag> {
    @Query("select c.id from playnite_tag c order by c.id asc")
    Flux<Long> findAllIds();
}
