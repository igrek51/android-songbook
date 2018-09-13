package igrek.songbook.service.string

import java.util.*

class StringSimplifier {

    companion object {

        private val locale = Locale("pl", "PL")
        private val specialCharsMapping = mutableMapOf<Char, Char>()

        init {
            // special polish letters transformation
            specialCharsMapping['ą'] = 'a'
            specialCharsMapping['ż'] = 'z'
            specialCharsMapping['ś'] = 's'
            specialCharsMapping['ź'] = 'z'
            specialCharsMapping['ę'] = 'e'
            specialCharsMapping['ć'] = 'c'
            specialCharsMapping['ń'] = 'n'
            specialCharsMapping['ó'] = 'o'
            specialCharsMapping['ł'] = 'l'
        }

        fun simplify(s: String): String {
            var s2 = s.toLowerCase(locale)
            // special chars mapping
            specialCharsMapping.forEach { (k, v) -> s2 = s2.replace(k, v) }
            return s2
        }

    }

}
