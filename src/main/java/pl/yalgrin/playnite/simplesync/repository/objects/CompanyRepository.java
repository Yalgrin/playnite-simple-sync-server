package pl.yalgrin.playnite.simplesync.repository.objects;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.stereotype.Repository;
import pl.yalgrin.playnite.simplesync.domain.objects.Company;
import reactor.core.publisher.Flux;

@Repository
public interface CompanyRepository extends ObjectRepository<Company> {
    @Query("select c.id from playnite_company c order by c.id asc")
    Flux<Long> findAllIds();
}
