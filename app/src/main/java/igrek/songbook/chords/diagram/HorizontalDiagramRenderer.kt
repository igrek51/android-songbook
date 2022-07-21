package igrek.songbook.chords.diagram

class HorizontalDiagramRenderer : DiagramRenderer {

    override fun render(frets: List<DisplayStringFret>): String {
        return frets.joinToString(separator = "\n") { it.display() }
    }

    private fun DisplayStringFret.display(): String {
        var output = "$stringName $fretSign"
        for (i in 0 until fretsShown) {
            output += "|"
            if (i == fingerPosition) {
                output += fingerValue
                if (fingerValue!! < 10 && digitsCount == 2)
                    output += "-"
            } else {
                output += "-".repeat(digitsCount)
            }
        }
        return "$output|"
    }
}