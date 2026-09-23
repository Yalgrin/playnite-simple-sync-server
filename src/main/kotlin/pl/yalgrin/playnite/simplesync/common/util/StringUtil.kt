package pl.yalgrin.playnite.simplesync.common.util

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
fun String?.isNotNullAndNotBlank(): Boolean {
    contract {
        returns(true) implies (this@isNotNullAndNotBlank != null)
    }
    return !isNullOrBlank()
}

fun String.escapeLike(): String {
    val value = this.lowercase()
    return buildString(value.length) {
        value.forEach { ch ->
            when (ch) {
                '\\', '%', '_' -> {
                    append('\\')
                    append(ch)
                }

                else -> append(ch)
            }
        }
    }
}