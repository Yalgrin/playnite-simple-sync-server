package pl.yalgrin.playnite.simplesync.util.objects

import pl.yalgrin.playnite.simplesync.dto.objects.CompanyDTO

class CompanyFactoryUtil {
    static CompanyDTO createCompany(String id, String name, boolean removed = false) {
        return CompanyDTO.builder()
                .id(id)
                .name(name)
                .removed(removed)
                .build()
    }

    static CompanyDTO randomCompany() {
        return CompanyDTO.builder()
                .id(UUID.randomUUID().toString())
                .name(UUID.randomUUID().toString())
                .build()
    }

    static CompanyDTO companyWithIndex(int idx) {
        return CompanyDTO.builder()
                .id("id-$idx")
                .name("name-$idx")
                .build()
    }
}
