package igrek.songbook.chordsv2.diagram

class VerticalDiagramRenderer : DiagramRenderer {

    override fun render(frets: List<DisplayStringFret>): String {
        val display2d = frets.reversed()
                .mapIndexed { index, it -> it.display(index == frets.lastIndex) }

        val flatRows = MutableList(display2d.first().size) { "" }
        display2d.forEach { column ->
            column.forEachIndexed { index, cell ->
                flatRows[index] += cell
            }
        }

        return flatRows.joinToString(separator = "\n")
    }

    private fun DisplayStringFret.display(last: Boolean): List<String> {
        val lines = mutableListOf<String>()
        val padding1 = when {
            last -> ""
            digitsCount == 2 -> "  "
            else -> " "
        }
        val dashPadding = if (last) "" else "-".repeat(digitsCount)
        lines.add(stringName + padding1)
        lines.add(fretSign + padding1)
        lines.add("-$dashPadding")

        for (i in 0 until fretsShown) {
            if (i == fingerPosition) {
                var fingerLine = fingerValue.toString()
                if (!last) {
                    fingerLine += " "
                    if (fingerValue!! < 10 && digitsCount == 2)
                        fingerLine += " "
                }
                lines.add(fingerLine)
            } else {
                lines.add("|$padding1")
            }
            lines.add("-$dashPadding")
        }

        return lines
    }
}