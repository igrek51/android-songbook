package igrek.songbook.chords.diagram

import android.app.Activity
import android.content.Context
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.widget.TextView
import igrek.songbook.R
import igrek.songbook.chords.ChordsConverter
import igrek.songbook.chords.syntax.chordsPrimaryDelimiters
import igrek.songbook.chords.syntax.chordsSupplementaryDelimiters
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.UiResourceService
import igrek.songbook.layout.contextmenu.ContextMenuBuilder
import igrek.songbook.settings.chordsnotation.ChordsNotation
import igrek.songbook.settings.instrument.ChordsInstrument
import igrek.songbook.settings.instrument.ChordsInstrumentService
import igrek.songbook.songpreview.lyrics.LyricsModel
import igrek.songbook.songpreview.lyrics.LyricsTextType
import javax.inject.Inject


class ChordsDiagramsService {

    @Inject
    lateinit var uiInfoService: UiInfoService
    @Inject
    lateinit var uiResourceService: UiResourceService
    @Inject
    lateinit var contextMenuBuilder: ContextMenuBuilder
    @Inject
    lateinit var activity: Activity
    @Inject
    lateinit var chordsInstrumentService: ChordsInstrumentService

    private val toEnglishConverter = ChordsConverter(ChordsNotation.GERMAN, ChordsNotation.ENGLISH)

    init {
        DaggerIoc.factoryComponent.inject(this)
    }

    private fun findUniqueChords(crdModel: LyricsModel): Set<String> {
        val instrument = chordsInstrumentService.instrument
        val chordDiagramCodes = getChordDiagrams(instrument)

        val uniqueChords = sortedSetOf<String>()
        crdModel.lines.forEach { line ->
            line.fragments
                    .filter { it.type == LyricsTextType.CHORDS }
                    .forEach { fragment ->
                        // split by primary delimiters first
                        fragment.text.split(*chordsPrimaryDelimiters).forEach { chord ->
                            val engChord = toEnglishConverter.convertChord(chord)
                            if (engChord in chordDiagramCodes) {
                                uniqueChords.add(chord)
                            } else {
                                // split further if not recognized
                                chord.split(*chordsSupplementaryDelimiters).forEach { subchord ->
                                    val subEngChord = toEnglishConverter.convertChord(subchord)
                                    if (subEngChord in chordDiagramCodes) {
                                        uniqueChords.add(subchord)
                                    }
                                }
                            }
                        }
                    }
        }

        return uniqueChords
    }

    private fun chordGraphs(chord: String): String {
        val instrument = chordsInstrumentService.instrument
        val diagramBuilder = ChordDiagramBuilder(instrument)
        val engChord = toEnglishConverter.convertChord(chord)
        val chordDiagramCodes = getChordDiagrams(instrument)
        return chordDiagramCodes[engChord]
                ?.joinToString(separator = "\n\n\n") { diagramBuilder.buildDiagram(it) }
                ?: ""
    }

    private fun getChordDiagrams(instrument: ChordsInstrument): Map<String, List<String>> {
        return when (instrument) {
            ChordsInstrument.GUITAR -> allGuitarChordsDiagrams.get()
            ChordsInstrument.UKULELE -> allUkuleleChordsDiagrams.get()
            ChordsInstrument.MANDOLIN -> allMandolinChordsDiagrams.get()
        }
    }

    fun showLyricsChordsMenu(crdModel: LyricsModel) {
        val uniqueChords = findUniqueChords(crdModel)
        if (uniqueChords.isEmpty()) {
            uiInfoService.showInfo(R.string.no_chords_recognized_in_song)
            return
        }

        showUniqueChordsMenu(uniqueChords)
    }

    private fun showUniqueChordsMenu(uniqueChords: Set<String>) {
        val actions = uniqueChords.map { chord ->
            ContextMenuBuilder.Action(chord) {
                showChordDefinition(chord, uniqueChords)
            }
        }.toList()

        contextMenuBuilder.showContextMenu(R.string.choose_a_chord, actions)
    }

    private fun showChordDefinition(chord: String, uniqueChords: Set<String>) {
        val message = chordGraphs(chord)
        val instrument = chordsInstrumentService.instrument
        val instrumentName = uiResourceService.resString(instrument.displayNameResId)
        val title = uiResourceService.resString(R.string.chord_diagrams_versions, chord, instrumentName)

        val alertBuilder = AlertDialog.Builder(activity)
        alertBuilder.setTitle(title)
        alertBuilder.setCancelable(true)

        alertBuilder.setPositiveButton(uiResourceService.resString(R.string.action_close)) { _, _ -> }
        alertBuilder.setNeutralButton(uiResourceService.resString(R.string.action_back)) { _, _ ->
            showUniqueChordsMenu(uniqueChords)
        }

        val inflater = activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val diagramView = inflater.inflate(R.layout.chord_diagrams, null, false)
        val diagramContent = diagramView.findViewById<TextView>(R.id.chordDiagramContent)
        diagramContent.text = message
        alertBuilder.setView(diagramView)

        alertBuilder.create().show()
    }

}