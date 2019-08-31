package igrek.songbook.chords.diagram

import android.app.Activity
import android.content.Context
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.widget.TextView
import igrek.songbook.R
import igrek.songbook.chords.ChordsConverter
import igrek.songbook.chords.syntax.chordsPrimaryDelimiters
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.UiResourceService
import igrek.songbook.layout.contextmenu.ContextMenuBuilder
import igrek.songbook.settings.chordsnotation.ChordsNotation
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

    val toEnglishConverter = ChordsConverter(ChordsNotation.GERMAN, ChordsNotation.ENGLISH)

    init {
        DaggerIoc.factoryComponent.inject(this)
    }

    fun findUniqueChords(crdModel: LyricsModel): Set<String> {
        val uniqueChords = sortedSetOf<String>()

        crdModel.lines.forEach { line ->
            line.fragments
                    .filter { it.type == LyricsTextType.CHORDS }
                    .forEach { fragment ->
                        // split by primary delimiters first
                        fragment.text.split(*chordsPrimaryDelimiters).forEach { chord ->
                            val engChord = toEnglishConverter.convertChord(chord)
                            if (engChord in allChordsDiagrams.get()) {
                                uniqueChords.add(chord)
                            } else {
                                // split further if not recognized
                                chord.split(*chordsPrimaryDelimiters).forEach { subchord ->
                                    val subEngChord = toEnglishConverter.convertChord(subchord)
                                    if (subEngChord in allChordsDiagrams.get()) {
                                        uniqueChords.add(subchord)
                            }
                        }
                    }
                }
            }
        }

        return uniqueChords
    }

    fun chordGraphs(chord: String): String {
        val diagramBuilder = ChordDiagramBuilder()
        val engChord = toEnglishConverter.convertChord(chord)
        return allChordsDiagrams.get()[engChord]
                ?.joinToString(separator = "\n\n\n") { diagramBuilder.buildDiagram(it) }
                ?: ""
    }

    fun showUniqueChordsMenu(crdModel: LyricsModel) {
        val uniqueChords = findUniqueChords(crdModel)
        if (uniqueChords.isEmpty()) {
            uiInfoService.showInfo(R.string.no_chords_recognized_in_song)
            return
        }

        val actions = uniqueChords.map { chord ->
            ContextMenuBuilder.Action(chord) {
                showChordDefinition(chord)
            }
        }.toList()

        contextMenuBuilder.showContextMenu(R.string.choose_a_chord, actions)
    }

    fun showChordDefinition(chord: String) {
        val message = chordGraphs(chord)
        val title = uiResourceService.resString(R.string.chord_diagrams_versions, chord)

        val alertBuilder = AlertDialog.Builder(activity)
        alertBuilder.setTitle(title)
        alertBuilder.setPositiveButton(uiResourceService.resString(R.string.action_info_ok)) { _, _ -> }
        alertBuilder.setCancelable(true)

        val inflater = activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val diagramView = inflater.inflate(R.layout.chord_diagrams, null, false)
        val diagramContent = diagramView.findViewById<TextView>(R.id.chordDiagramContent)
        diagramContent.text = message

        alertBuilder.setView(diagramView)

        val alertDialog = alertBuilder.create()
        alertDialog.show()
    }

}