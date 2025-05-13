package pl.yalgrin.playnite.simplesync.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.stereotype.Repository;
import pl.yalgrin.playnite.simplesync.domain.Category;
import reactor.core.publisher.Flux;

@Repository
public interface CategoryRepository extends ObjectRepository<Category> {
    @Query("select c.id from playnite_category c order by c.id asc")
    Flux<Long> findAllIds();
}
