package pl.yalgrin.playnite.simplesync.util

import java.security.MessageDigest

fun String.toSha1(): String {
    return sha1(this)
}

fun sha1(input: String): String {
    val digest = MessageDigest.getInstance("SHA-1")
    digest.update(input.toByteArray())
    return digest.digest().joinToString("") { "%02x".format(it) }
}