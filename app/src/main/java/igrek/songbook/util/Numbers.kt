package igrek.songbook.util

fun Int.limitTo(max: Int): Int {
    return if (this > max) max else this
}

fun Long.limitTo(max: Long): Long {
    return if (this > max) max else this
}

fun Float.limitTo(max: Float): Float {
    return if (this > max) max else this
}
