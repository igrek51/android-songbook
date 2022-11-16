package igrek.songbook.info.logger

class WrapContextError(private val _message: String, cause: Throwable) :
    RuntimeException(_message, cause) {

    override val message: String?
        get() = "$_message: ${cause?.message}"

}