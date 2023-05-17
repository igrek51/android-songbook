package igrek.songbook.system.locale

import java.util.Locale

class StringSimplifier {

    companion object {

        val locale = Locale("pl", "PL")

        private val specialCharsMapping: Map<Char, Char> = mapOf(
            'ą' to 'a',
            'ż' to 'z',
            'ś' to 's',
            'ź' to 'z',
            'ę' to 'e',
            'ć' to 'c',
            'ń' to 'n',
            'ó' to 'o',
            'ł' to 'l',
        )

        fun simplify(s: String): String {
            var s2 = s.lowercase(locale).replace("'", "")
            // special chars mapping
            specialCharsMapping.forEach { (k, v) -> s2 = s2.replace(k, v) }
            return s2
        }

    }

}
