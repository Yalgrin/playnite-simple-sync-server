package pl.yalgrin.playnite.simplesync.util.library

import pl.yalgrin.playnite.simplesync.library.dto.CompanyDTO

class CompanyFactoryUtil {
    static CompanyDTO createCompany(String id, String name, boolean removed = false) {
        return new CompanyDTO(null, id, name, removed, null, null, null, null)
    }

    static CompanyDTO randomCompany() {
        return createCompany(UUID.randomUUID().toString(), UUID.randomUUID().toString())
    }

    static CompanyDTO companyWithIndex(int idx) {
        return createCompany("id-$idx", "name-$idx")
    }
}
