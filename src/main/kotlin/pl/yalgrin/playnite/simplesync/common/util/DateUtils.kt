package pl.yalgrin.playnite.simplesync.common.util

import java.time.ZonedDateTime

fun ZonedDateTime?.max(other: ZonedDateTime?): ZonedDateTime? {
    if (this == null) {
        return other
    }
    if (other == null) {
        return this
    }
    return maxOf(this, other)
}