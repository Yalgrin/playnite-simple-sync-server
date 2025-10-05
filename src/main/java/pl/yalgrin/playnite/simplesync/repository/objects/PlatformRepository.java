package pl.yalgrin.playnite.simplesync.repository.objects;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.stereotype.Repository;
import pl.yalgrin.playnite.simplesync.domain.objects.Platform;
import reactor.core.publisher.Flux;

@Repository
public interface PlatformRepository extends ObjectRepository<Platform> {
    @Query("select c.id from playnite_platform c order by c.id asc")
    Flux<Long> findAllIds();

    @Query("select id from playnite_platform where id in (:ids)")
    Flux<Long> findIdsByIds(Iterable<Long> ids);
}
