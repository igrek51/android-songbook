package igrek.songbook.util

fun String.capitalize(): String {
    return this.replaceFirstChar { it.uppercaseChar() }
}

fun buildSongName(title: String, artist: String?): String {
    return if (artist.isNullOrEmpty()) {
        title
    } else {
        "$title - $artist"
    }
}
