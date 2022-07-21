package igrek.songbook.chords.diagram

import android.graphics.Bitmap
import igrek.songbook.chords.model.GeneralChord

interface DrawableChordDiagramBuilder {

    fun buildDiagram(engChord: String): Bitmap?

    fun hasDiagram(chord: GeneralChord): Boolean

}
