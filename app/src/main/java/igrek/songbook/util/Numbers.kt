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

fun Float.applyMin(min: Float): Float {
    return if (this < min) min else this
}

fun Float.limitBetween(min: Float, max: Float): Float {
    if (this < min)
        return min
    if (this > max)
        return max
    return this
}

fun Long.interpolate(min: Long, max: Long): Float {
    if (this < min)
        return 0f
    if (this > max)
        return 1f
    return (this - min).toFloat() / (max - min)
}

fun Float.cutOffMin(min: Float): Float {
    return if (this < min) min else this
}