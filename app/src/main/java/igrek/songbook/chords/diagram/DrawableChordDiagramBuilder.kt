package igrek.songbook.chords.diagram

import android.graphics.Bitmap

interface DrawableChordDiagramBuilder {

    fun buildDiagram(engChord: String): Bitmap?

}
