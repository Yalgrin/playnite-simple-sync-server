package pl.yalgrin.playnite.simplesync.util.panel

import pl.yalgrin.playnite.simplesync.panel.dto.PanelCompanyDTO

class PanelCompanyFactoryUtil {
    static PanelCompanyDTO createCompany(Long id, String name) {
        return new PanelCompanyDTO(id, name)
    }

    static PanelCompanyDTO createCompany(String name) {
        return new PanelCompanyDTO(null, name)
    }

    static PanelCompanyDTO randomCompany() {
        return createCompany(UUID.randomUUID().toString())
    }

    static PanelCompanyDTO companyWithIndex(int idx) {
        return createCompany("name-$idx")
    }
}
