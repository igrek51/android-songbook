package igrek.songbook.info.errorcheck

class ContextError(
    private val _context: String,
    cause: Throwable,
) : RuntimeException(_context, cause) {

    override val message: String
        get() = "$_context: ${cause?.message}"

}