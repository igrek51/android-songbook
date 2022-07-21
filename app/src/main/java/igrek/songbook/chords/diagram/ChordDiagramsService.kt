package igrek.songbook.chords.diagram


import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import igrek.songbook.R
import igrek.songbook.chords.converter.ChordsNotationConverter
import igrek.songbook.chords.detect.UniqueChordsFinder
import igrek.songbook.chords.diagram.guitar.ChordTextDiagramBuilder
import igrek.songbook.chords.diagram.piano.PianoChordDiagramBuilder
import igrek.songbook.chords.model.GeneralChord
import igrek.songbook.chords.model.LyricsModel
import igrek.songbook.chords.parser.ChordParser
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.UiResourceService
import igrek.songbook.info.errorcheck.SafeExecutor
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.layout.contextmenu.ContextMenuBuilder
import igrek.songbook.settings.chordsnotation.ChordsNotation
import igrek.songbook.settings.chordsnotation.ChordsNotationService
import igrek.songbook.settings.enums.ChordsInstrument
import igrek.songbook.settings.preferences.PreferencesState
import igrek.songbook.system.SoftKeyboardService
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@OptIn(DelicateCoroutinesApi::class)
class ChordDiagramsService(
    uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
    uiResourceService: LazyInject<UiResourceService> = appFactory.uiResourceService,
    contextMenuBuilder: LazyInject<ContextMenuBuilder> = appFactory.contextMenuBuilder,
    activity: LazyInject<Activity> = appFactory.activity,
    chordsNotationService: LazyInject<ChordsNotationService> = appFactory.chordsNotationService,
    preferencesState: LazyInject<PreferencesState> = appFactory.preferencesState,
    softKeyboardService: LazyInject<SoftKeyboardService> = appFactory.softKeyboardService,
) {
    private val uiInfoService by LazyExtractor(uiInfoService)
    private val uiResourceService by LazyExtractor(uiResourceService)
    private val contextMenuBuilder by LazyExtractor(contextMenuBuilder)
    private val activity by LazyExtractor(activity)
    private val chordsNotationService by LazyExtractor(chordsNotationService)
    private val preferencesState by LazyExtractor(preferencesState)
    private val softKeyboardService by LazyExtractor(softKeyboardService)

    private val logger = LoggerFactory.logger

    fun showLyricsChordsMenu(lyrics: LyricsModel) {
        val uniqueChords = UniqueChordsFinder().findUniqueChordNamesInLyrics(lyrics)
        if (uniqueChords.isEmpty()) {
            uiInfoService.showInfo(R.string.no_chords_recognized_in_song)
            return
        }
        showUniqueChordsMenu(uniqueChords)
    }

    private fun showUniqueChordsMenu(uniqueChords: Set<String>) {
        val actions = uniqueChords.map { chord ->
            ContextMenuBuilder.Action(chord) {
                showChordDiagramsAlert(chord, uniqueChords)
            }
        }.toList()
        contextMenuBuilder.showContextMenu(R.string.choose_a_chord, actions)
    }

    private fun showChordDiagramsAlert(typedChord: String, uniqueChords: Set<String>) {
        GlobalScope.launch(Dispatchers.Main) {
            SafeExecutor {

                val instrument = preferencesState.chordsInstrument
                val instrumentName = uiResourceService.resString(instrument.displayNameResId)
                val title = uiResourceService.resString(R.string.chord_diagrams_versions, typedChord, instrumentName)

                val alertBuilder = AlertDialog.Builder(activity)
                alertBuilder.setTitle(title)
                alertBuilder.setCancelable(true)

                alertBuilder.setPositiveButton(uiResourceService.resString(R.string.action_close)) { _, _ -> }
                if (uniqueChords.isNotEmpty()) {
                    alertBuilder.setNeutralButton(uiResourceService.resString(R.string.action_back)) { _, _ ->
                        showUniqueChordsMenu(uniqueChords)
                    }
                }
                alertBuilder.setNegativeButton(uiResourceService.resString(R.string.action_find_chord)) { _, _ ->
                    showFindChordByNameMenu()
                }

                val inflater = activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

                val diagramView = when (preferencesState.chordsInstrument) {
                    ChordsInstrument.PIANO -> inflateDrawableChordDiagramView(typedChord, inflater)
                    else -> inflateTextChordDiagramView(typedChord, inflater)
                }

                if (diagramView == null) {
                    uiInfoService.showInfo(R.string.chord_diagram_not_found)
                    return@SafeExecutor
                }

                alertBuilder.setView(diagramView)

                if (!activity.isFinishing) {
                    alertBuilder.create().show()
                }
            }
        }
    }

    fun showFindChordByNameMenu() {
        val dlgAlert = AlertDialog.Builder(activity)
        dlgAlert.setTitle(uiResourceService.resString(R.string.chord_diagram_find_chord))

        val input = EditText(activity)
        input.inputType = InputType.TYPE_CLASS_TEXT
        dlgAlert.setView(input)

        dlgAlert.setPositiveButton(uiResourceService.resString(R.string.action_find_chord)) { _, _ ->
            tryToFindChordDiagram(input.text.toString())
        }
        dlgAlert.setNegativeButton(uiResourceService.resString(R.string.action_cancel)) { _, _ -> }
        dlgAlert.setCancelable(true)
        val dialog = dlgAlert.create()
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        dialog.show()

        input.requestFocus()
        Handler(Looper.getMainLooper()).post {
            softKeyboardService.showSoftKeyboard(input)
        }
    }

    private fun tryToFindChordDiagram(_chordName: String) {
        val typedChordName = _chordName.trim()
        if (typedChordName.isBlank()) {
            uiInfoService.showInfo(R.string.chord_diagram_not_found)
            return
        }
        if (!hasChordDiagram(typedChordName)) {
            uiInfoService.showInfo(R.string.chord_diagram_not_found)
            return
        }

        showChordDiagramsAlert(typedChordName, emptySet())
    }

    private fun hasChordDiagram(typedChordName: String): Boolean {
        return when (preferencesState.chordsInstrument) {
            ChordsInstrument.PIANO -> {
                val chord: GeneralChord? = ChordParser(chordsNotationService.chordsNotation).parseGeneralChord(typedChordName)
                if (chord == null) {
                    false
                } else {
                    PianoChordDiagramBuilder().hasDiagram(chord)
                }
            }
            else -> {
                val toEnglishConverter = ChordsNotationConverter(chordsNotationService.chordsNotation, ChordsNotation.ENGLISH, false)
                val engChord = toEnglishConverter.convertChordFragments(typedChordName)
                val chordDiagramCodes = getChordDiagramCodes(preferencesState.chordsInstrument)
                engChord in chordDiagramCodes
            }
        }
    }

    private fun getChordDiagramCodes(instrument: ChordsInstrument): Map<String, List<String>> {
        return when (instrument) {
            ChordsInstrument.GUITAR -> allGuitarChordsDiagrams
            ChordsInstrument.UKULELE -> allUkuleleChordsDiagrams
            ChordsInstrument.MANDOLIN -> allMandolinChordsDiagrams
            ChordsInstrument.PIANO -> allPianoChordsNames
        }
    }

    private fun inflateTextChordDiagramView(typedChord: String, inflater: LayoutInflater): View {
        val diagramView = inflater.inflate(R.layout.component_chord_text_diagrams, null, false)
        val diagramContent = diagramView.findViewById<TextView>(R.id.chordDiagramContent)

        val toEnglishConverter = ChordsNotationConverter(chordsNotationService.chordsNotation, ChordsNotation.ENGLISH, preferencesState.forceSharpNotes)
        val engChord: String = toEnglishConverter.convertChordFragments(typedChord)

        val instrument = preferencesState.chordsInstrument
        val diagramBuilder = ChordTextDiagramBuilder(instrument, preferencesState.chordDiagramStyle)

        val chordDiagramCodes = getChordDiagramCodes(instrument)
        val message = chordDiagramCodes[engChord]
            ?.joinToString(separator = "\n\n\n") { diagramBuilder.buildDiagram(it) }
            ?: ""

        diagramContent.text = message
        return diagramView
    }

    private fun inflateDrawableChordDiagramView(typedChord: String, inflater: LayoutInflater): View? {
        val diagramView = inflater.inflate(R.layout.component_chord_drawable_diagram, null, false)
        val diagramImage = diagramView.findViewById<ImageView>(R.id.chordDiagramImage)

        val toEnglishConverter = ChordsNotationConverter(chordsNotationService.chordsNotation, ChordsNotation.ENGLISH, preferencesState.forceSharpNotes)
        val engChord: String = toEnglishConverter.convertChordFragments(typedChord)

        val diagramBuilder: DrawableChordDiagramBuilder = when (preferencesState.chordsInstrument) {
            ChordsInstrument.PIANO -> PianoChordDiagramBuilder()
            else -> throw RuntimeException("Unsupported instrument")
        }

        val bitmap = diagramBuilder.buildDiagram(engChord) ?: return null

        diagramImage.setImageBitmap(bitmap)
        return diagramView
    }

}