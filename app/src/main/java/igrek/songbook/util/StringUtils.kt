package igrek.songbook.util

fun String.capitalize(): String {
    return this.replaceFirstChar { it.uppercaseChar() }
}
