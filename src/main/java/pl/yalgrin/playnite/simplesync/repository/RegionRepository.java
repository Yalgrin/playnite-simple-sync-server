package pl.yalgrin.playnite.simplesync.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.stereotype.Repository;
import pl.yalgrin.playnite.simplesync.domain.Region;
import reactor.core.publisher.Flux;

@Repository
public interface RegionRepository extends ObjectRepository<Region> {
    @Query("select c.id from playnite_region c order by c.id asc")
    Flux<Long> findAllIds();
}
