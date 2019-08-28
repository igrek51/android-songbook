package igrek.songbook.chords.diagram

import igrek.songbook.R
import igrek.songbook.chords.syntax.chordsPrimaryDelimiters
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.UiResourceService
import igrek.songbook.layout.contextmenu.ContextMenuBuilder
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

    init {
        DaggerIoc.factoryComponent.inject(this)
    }

    fun findUniqueChords(crdModel: LyricsModel): Set<String> {
        val uniqueChords = sortedSetOf<String>()

        crdModel.lines.forEach { line ->
            line.fragments.forEach { fragment ->
                if (fragment.type == LyricsTextType.CHORDS) {
                    // split by primary delimiters first
                    fragment.text.split(*chordsPrimaryDelimiters).forEach { chord ->
                        if (chord in chordsDiagrams) {
                            uniqueChords.add(chord)
                        } else {
                            // split further if not recognized
                            chord.split(*chordsPrimaryDelimiters).forEach { subchord ->
                                if (subchord in chordsDiagrams) {
                                    uniqueChords.add(subchord)
                                }
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
        return chordsDiagrams[chord]
                ?.joinToString(separator = "\n\n") { diagramBuilder.buildDiagram(it) }
                ?: ""
    }

    fun showUniqueChordsMenu(crdModel: LyricsModel) {
        val uniqueChords = findUniqueChords(crdModel)
        if (uniqueChords.isEmpty()) {
            uiInfoService.showInfo(R.string.no_chords_found_in_song)
            return
        }

        val actions = uniqueChords.map { chord ->
            ContextMenuBuilder.Action(chord) {
                showChordDefinition(chord)
            }
        }.toList()

        contextMenuBuilder.showContextMenu(R.string.choose_playlist, actions)
    }

    fun showChordDefinition(chord: String) {
        val message = chordGraphs(chord)
        uiInfoService.showDialog(chord, message)
    }

}