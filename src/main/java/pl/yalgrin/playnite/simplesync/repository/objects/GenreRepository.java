package pl.yalgrin.playnite.simplesync.repository.objects;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.stereotype.Repository;
import pl.yalgrin.playnite.simplesync.domain.objects.Genre;
import reactor.core.publisher.Flux;

@Repository
public interface GenreRepository extends ObjectRepository<Genre> {
    @Query("select c.id from playnite_genre c order by c.id asc")
    Flux<Long> findAllIds();
}
