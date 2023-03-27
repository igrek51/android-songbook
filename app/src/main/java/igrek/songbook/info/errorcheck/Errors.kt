package igrek.songbook.info.errorcheck

fun formatErrorMessage(t: Throwable): String {
    val message = t.message
    val cause = t.cause
    return when {
        t is ContextError -> message.orEmpty()
        message != null && cause != null -> message + ": " + formatErrorMessage(cause)
        message != null -> message
        cause != null -> t::class.simpleName.orEmpty() + ": " + formatErrorMessage(cause)
        else -> t::class.simpleName.orEmpty()
    }
}
