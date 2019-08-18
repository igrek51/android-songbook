package igrek.songbook.persistence.general.model

import java.text.Collator
import java.util.*

object CategoryNameComparator : Comparator<Category> {

    private val locale = Locale("pl", "PL")
    private val stringCollator = Collator.getInstance(locale)

    override fun compare(lhs: Category?, rhs: Category?): Int {
        val lName = lhs?.displayName?.toLowerCase(locale)
        val rName = rhs?.displayName?.toLowerCase(locale)
        return stringCollator.compare(lName, rName)
    }
}