package pl.yalgrin.playnite.simplesync.web.library

import org.springframework.web.bind.annotation.*
import pl.yalgrin.playnite.simplesync.dto.objects.CompanyDTO
import pl.yalgrin.playnite.simplesync.helper.SingleExecutorHelper
import pl.yalgrin.playnite.simplesync.service.objects.CompanyService
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/company")
class CompanyResource(
    private val service: CompanyService,
    private val singleExecutorHelper: SingleExecutorHelper
) {

    @GetMapping("/{id}")
    fun getCompany(@PathVariable id: Long): Mono<CompanyDTO> {
        return service.findById(id)
    }

    @PostMapping("/save")
    fun saveCompany(@RequestBody dto: CompanyDTO): Mono<CompanyDTO> {
        return singleExecutorHelper.runOnExecutor(service.saveObject(dto))
    }

    @PostMapping("/delete")
    fun deleteCompany(@RequestBody dto: CompanyDTO): Mono<Void> {
        return singleExecutorHelper.runOnExecutor(service.deleteObject(dto))
    }
}