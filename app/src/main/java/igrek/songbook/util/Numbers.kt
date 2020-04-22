package igrek.songbook.util

fun Int.limitTo(max: Int): Int {
    return if (this > max) max else this
}
