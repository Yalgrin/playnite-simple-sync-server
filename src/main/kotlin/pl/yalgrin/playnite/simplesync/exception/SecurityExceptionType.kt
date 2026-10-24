package pl.yalgrin.playnite.simplesync.exception

enum class SecurityExceptionType {
    UNAUTHORIZED,
    FORBIDDEN,
    NO_VALID_CLIENT_SESSION,
    MISSING_REGISTRATION,
    INVALID_REGISTRATION,
    CLIENT_ALREADY_REGISTERED
}