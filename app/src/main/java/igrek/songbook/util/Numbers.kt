package igrek.songbook.util

import java.math.RoundingMode
import java.text.DecimalFormat

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

val decimalFormat1: DecimalFormat = DecimalFormat("#.#").apply {
    roundingMode = RoundingMode.HALF_UP
}

val decimalFormat3: DecimalFormat = DecimalFormat("#.###").apply {
    roundingMode = RoundingMode.HALF_UP
}

fun roundDecimal3(value: Float): String {
    return decimalFormat3.format(value.toDouble())
}

fun roundDecimal1(value: Float): String {
    return decimalFormat1.format(value.toDouble())
}

fun Int.clampMin(min: Int): Int {
    return if (this < min) min else this
}

fun Int.clampMax(max: Int): Int {
    return if (this > max) max else this
}

fun Int.clamp(min: Int, max: Int): Int {
    if (this < min)
        return min
    if (this > max)
        return max
    return this
}