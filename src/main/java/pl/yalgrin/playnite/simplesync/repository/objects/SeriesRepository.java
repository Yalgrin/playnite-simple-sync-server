package pl.yalgrin.playnite.simplesync.repository.objects;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.stereotype.Repository;
import pl.yalgrin.playnite.simplesync.domain.objects.Series;
import reactor.core.publisher.Flux;

@Repository
public interface SeriesRepository extends ObjectRepository<Series> {
    @Query("select c.id from playnite_series c order by c.id asc")
    Flux<Long> findAllIds();
}
