package pl.yalgrin.playnite.simplesync.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.stereotype.Repository;
import pl.yalgrin.playnite.simplesync.domain.FilterPreset;
import reactor.core.publisher.Flux;

@Repository
public interface FilterPresetRepository extends ObjectRepository<FilterPreset> {
    @Query("select c.id from playnite_filter_preset c order by c.id asc")
    Flux<Long> findAllIds();
}
