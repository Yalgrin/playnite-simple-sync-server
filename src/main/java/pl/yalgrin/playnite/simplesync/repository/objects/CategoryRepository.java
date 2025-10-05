package pl.yalgrin.playnite.simplesync.repository.objects;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.stereotype.Repository;
import pl.yalgrin.playnite.simplesync.domain.objects.Category;
import reactor.core.publisher.Flux;

@Repository
public interface CategoryRepository extends ObjectRepository<Category> {
    @Query("select c.id from playnite_category c order by c.id asc")
    Flux<Long> findAllIds();
}
