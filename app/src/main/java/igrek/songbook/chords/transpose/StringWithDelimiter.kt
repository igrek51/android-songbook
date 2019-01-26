package igrek.songbook.chords.transpose

internal class StringWithDelimiter {

    var str: String
    var delimiter: String

    constructor(str: String, delimiter: String) {
        this.str = str
        this.delimiter = delimiter
    }

    constructor(str: String) {
        this.str = str
        this.delimiter = ""
    }
}
