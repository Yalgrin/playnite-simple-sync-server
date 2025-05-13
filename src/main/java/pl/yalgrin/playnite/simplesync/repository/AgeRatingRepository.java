package pl.yalgrin.playnite.simplesync.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.stereotype.Repository;
import pl.yalgrin.playnite.simplesync.domain.AgeRating;
import reactor.core.publisher.Flux;

@Repository
public interface AgeRatingRepository extends ObjectRepository<AgeRating> {
    @Query("select c.id from playnite_age_rating c order by c.id asc")
    Flux<Long> findAllIds();
}
