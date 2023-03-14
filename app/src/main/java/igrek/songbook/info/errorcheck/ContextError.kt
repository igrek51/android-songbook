package igrek.songbook.info.errorcheck

class ContextError(private val _message: String, cause: Throwable) :
    RuntimeException(_message, cause) {

    override val message: String
        get() = "$_message: ${cause?.message}"

}