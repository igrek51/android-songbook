package igrek.songbook.util

import java.text.SimpleDateFormat
import java.util.*

fun String.capitalize(): String {
    return this.replaceFirstChar { it.uppercaseChar() }
}

fun todayDateText(): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
    val date = Date()
    return dateFormat.format(date)
}
