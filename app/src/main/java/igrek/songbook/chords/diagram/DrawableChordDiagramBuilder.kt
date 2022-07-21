package igrek.songbook.chords.diagram

import android.content.Context
import android.graphics.Bitmap

interface DrawableChordDiagramBuilder {

    fun buildDiagram(engChord: String, context: Context): Bitmap

}
