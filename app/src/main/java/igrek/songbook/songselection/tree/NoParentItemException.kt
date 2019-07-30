package igrek.songbook.songselection.tree

class NoParentItemException : Exception {

    constructor() : super()

    constructor(detailMessage: String) : super(detailMessage)
}
