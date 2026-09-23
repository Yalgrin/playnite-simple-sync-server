package pl.yalgrin.playnite.simplesync.library.enums


enum class InstallSizeGroup(val value: Int) {
    NONE(0),
    S0_0MB_100MB(1),
    S1_100MB_1GB(2),
    S2_1GB_5GB(3),
    S3_5GB_10GB(4),
    S4_10GB_20GB(5),
    S5_20GB_40GB(6),
    S6_40GB_100GB(7),
    S7_100GB_PLUS(8)
}

fun Int.toInstallSizeGroupById(): InstallSizeGroup? {
    return InstallSizeGroup.entries.firstOrNull { it.value == this }
}

fun Long?.toInstallSizeGroup(): InstallSizeGroup {
    return when {
        this == null || this == 0L -> {
            InstallSizeGroup.NONE
        }

        this <= 0x6400000 -> { //100MB
            InstallSizeGroup.S0_0MB_100MB
        }

        this <= 0x40000000 -> { //1GB
            InstallSizeGroup.S1_100MB_1GB
        }

        this <= 0x140000000 -> { //5GB
            InstallSizeGroup.S2_1GB_5GB
        }

        this <= 0x280000000 -> { //10GB
            InstallSizeGroup.S3_5GB_10GB
        }

        this <= 0x500000000 -> { //20GB
            InstallSizeGroup.S4_10GB_20GB
        }

        this <= 0xA00000000 -> { //40GB
            InstallSizeGroup.S5_20GB_40GB
        }

        this <= 0x1900000000 -> { //100GB
            InstallSizeGroup.S6_40GB_100GB
        }

        else -> {
            InstallSizeGroup.S7_100GB_PLUS
        }
    }
}