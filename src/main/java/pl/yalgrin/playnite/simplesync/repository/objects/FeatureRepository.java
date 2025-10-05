package pl.yalgrin.playnite.simplesync.repository.objects;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.stereotype.Repository;
import pl.yalgrin.playnite.simplesync.domain.objects.Feature;
import reactor.core.publisher.Flux;

@Repository
public interface FeatureRepository extends ObjectRepository<Feature> {
    @Query("select c.id from playnite_feature c order by c.id asc")
    Flux<Long> findAllIds();
}
