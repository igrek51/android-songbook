package igrek.songbook.chords.diagram.guitar

import igrek.songbook.settings.enums.ChordsInstrument
import kotlin.math.max


class ChordTextDiagramBuilder(
    private val instrument: ChordsInstrument = ChordsInstrument.default,
    private val chordDiagramStyle: ChordDiagramStyle = ChordDiagramStyle.Horizontal,
) {

    private val minimumFretsShown = 3
    private val stringsNames = mapOf(
        ChordsInstrument.GUITAR to listOf("E", "H", "G", "D", "A", "E"),
        ChordsInstrument.UKULELE to listOf("A", "E", "C", "G"),
        ChordsInstrument.MANDOLIN to listOf("E", "A", "D", "G")
    )

    fun buildDiagram(definition: String): String {
        val fretsStr = definition.split(",")

        val stringsCount = stringsNames.getValue(instrument).size
        check(fretsStr.size == stringsCount) { "invalid frets size" }

        val frets = fretsStr.map {
            if (it == "x")
                -1
            else
                it.toInt()
        }

        return buildDiagram(frets)
    }

    private fun buildDiagram(frets: List<Int>): String {
        val positives = frets.filter { it > 0 }
        val minFinger = positives.minOrNull() ?: 0
        val maxFinger = positives.maxOrNull() ?: 0

        val fingersRange = maxFinger - minFinger + 1
        val showEllipsis = maxFinger > 3
        val digitsCount = if (maxFinger >= 10) 2 else 1
        val fretsShown = max(fingersRange, minimumFretsShown)
        val fretSign = if (showEllipsis) "…" else " "
        val hiddenFrets = if (showEllipsis) minFinger - 1 else 0

        val displayStringFrets: List<DisplayStringFret> = stringsNames.getValue(instrument).map { stringName ->
            DisplayStringFret(
                    stringName = stringName,
                    fretSign = fretSign,
                    fretsShown = fretsShown,
                    digitsCount = digitsCount
            )
        }.toMutableList()

        frets.reversed().forEachIndexed { index, fretValue ->
            val displayFret = displayStringFrets[index]
            when (fretValue) {
                -1 -> displayFret.fretSign = "x"
                0 -> displayFret.fretSign = "0"
                else -> {
                    displayFret.fingerValue = fretValue
                    displayFret.fingerPosition = fretValue - hiddenFrets - 1
                }
            }
        }

        val renderer = when (chordDiagramStyle) {
            ChordDiagramStyle.Horizontal -> HorizontalDiagramRenderer()
            ChordDiagramStyle.Vertical -> VerticalDiagramRenderer()
        }
        return renderer.render(displayStringFrets)
    }

}

interface DiagramRenderer {
    fun render(frets: List<DisplayStringFret>): String
}

data class DisplayStringFret(
    val stringName: String,
    var fretSign: String = " ",
    val fretsShown: Int,
    val digitsCount: Int = 1,
    var fingerPosition: Int? = null,
    var fingerValue: Int? = null,
)