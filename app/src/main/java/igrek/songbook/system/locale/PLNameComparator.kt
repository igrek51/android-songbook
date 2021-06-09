package igrek.songbook.system.locale

import java.text.Collator
import java.util.*

object PLNameComparator : Comparator<String> {

    private val locale = Locale("pl", "PL")
    private val stringCollator = Collator.getInstance(locale)

    override fun compare(lhs: String?, rhs: String?): Int {
        val lName = lhs?.lowercase(locale)
        val rName = rhs?.lowercase(locale)
        return stringCollator.compare(lName, rName)
    }
}