package pl.yalgrin.playnite.simplesync.web.panel

import org.springframework.web.bind.annotation.*
import pl.yalgrin.playnite.simplesync.helper.SingleExecutorHelper
import pl.yalgrin.playnite.simplesync.library.service.CompanyService
import pl.yalgrin.playnite.simplesync.panel.dto.PanelCompanyDTO
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/panel/company")
class PanelCompanyResource(
    private val service: CompanyService,
    private val singleExecutorHelper: SingleExecutorHelper
) {

    @GetMapping("/{id}")
    fun getCompany(@PathVariable id: Long): Mono<PanelCompanyDTO> {
        return service.findByIdForPanel(id)
    }

    @PostMapping("/save")
    fun saveCompany(@RequestBody dto: PanelCompanyDTO): Mono<PanelCompanyDTO> {
        return singleExecutorHelper.runOnExecutor(service.saveObjectAndPublishChanges(dto))
    }

    @PostMapping("/delete")
    fun deleteCompany(@RequestBody dto: PanelCompanyDTO): Mono<Unit> {
        return singleExecutorHelper.runOnExecutor(service.deleteObjectAndPublishChanges(dto))
    }

    @PostMapping("/filter")
    fun filterCompanies(): Mono<List<PanelCompanyDTO>> {
        return service.findAllForPanel()
    }
}
