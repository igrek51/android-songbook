package igrek.songbook.custom

class ChordsValidationError(val messageResId: Int?, val errorMessage: String?) : RuntimeException() {

    constructor(messageResId: Int) : this(messageResId, null)

    constructor(errorMessage: String) : this(null, errorMessage)

}