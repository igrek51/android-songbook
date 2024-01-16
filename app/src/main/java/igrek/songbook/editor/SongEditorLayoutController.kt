package igrek.songbook.editor

import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import igrek.songbook.R
import igrek.songbook.about.WebviewLayoutController
import igrek.songbook.admin.AdminService
import igrek.songbook.compose.AppTheme
import igrek.songbook.custom.CustomSongService
import igrek.songbook.custom.SongImportFileChooser
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.UiResourceService
import igrek.songbook.info.errorcheck.SafeClickListener
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.layout.InflatedLayout
import igrek.songbook.layout.LayoutController
import igrek.songbook.layout.contextmenu.ContextMenuBuilder
import igrek.songbook.layout.dialog.ConfirmDialogBuilder
import igrek.songbook.layout.navigation.NavigationMenuController
import igrek.songbook.persistence.general.model.Song
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.settings.chordsnotation.ChordsNotation
import igrek.songbook.settings.chordsnotation.ChordsNotationService
import igrek.songbook.settings.preferences.SettingsState
import igrek.songbook.songpreview.SongOpener
import igrek.songbook.system.SoftKeyboardService
import igrek.songbook.system.locale.StringSimplifier

class SongEditorLayoutController(
    uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
    customSongService: LazyInject<CustomSongService> = appFactory.customSongService,
    softKeyboardService: LazyInject<SoftKeyboardService> = appFactory.softKeyboardService,
    songImportFileChooser: LazyInject<SongImportFileChooser> = appFactory.songImportFileChooser,
    chordsNotationService: LazyInject<ChordsNotationService> = appFactory.chordsNotationService,
    contextMenuBuilder: LazyInject<ContextMenuBuilder> = appFactory.contextMenuBuilder,
    settingsState: LazyInject<SettingsState> = appFactory.settingsState,
    songsRepository: LazyInject<SongsRepository> = appFactory.songsRepository,
    adminService: LazyInject<AdminService> = appFactory.adminService,
    webviewLayoutController: LazyInject<WebviewLayoutController> = appFactory.webviewLayoutController,
    songOpener: LazyInject<SongOpener> = appFactory.songOpener,
    layoutController: LazyInject<LayoutController> = appFactory.layoutController,
    uiResourceService: LazyInject<UiResourceService> = appFactory.uiResourceService,
    appCompatActivity: LazyInject<AppCompatActivity> = appFactory.appCompatActivity,
    navigationMenuController: LazyInject<NavigationMenuController> = appFactory.navigationMenuController,
) : InflatedLayout(
    _layoutResourceId = R.layout.screen_custom_song_details
) {
    private val uiInfoService by LazyExtractor(uiInfoService)
    private val customSongService by LazyExtractor(customSongService)
    private val softKeyboardService by LazyExtractor(softKeyboardService)
    private val songImportFileChooser by LazyExtractor(songImportFileChooser)
    private val chordsNotationService by LazyExtractor(chordsNotationService)
    private val contextMenuBuilder by LazyExtractor(contextMenuBuilder)
    val preferencesState by LazyExtractor(settingsState)
    private val songsRepository by LazyExtractor(songsRepository)
    private val adminService by LazyExtractor(adminService)
    private val webviewLayoutController by LazyExtractor(webviewLayoutController)
    private val songOpener by LazyExtractor(songOpener)
    private val uiResourceService by LazyExtractor(uiResourceService)
    private val navigationMenuController by LazyExtractor(navigationMenuController)

    private var currentSong: Song? = null
    private var allCategoryNames: List<String> = emptyList()
    val chordNotationOptions: List<ChordsNotation> = ChordsNotation.entries
    val state = LayoutState()

    private val history = LyricsEditorHistory()
    private val textEditor: ITextEditor = EditTextTextEditor(state.lyricsContent)
    val editorTransformer: ChordsEditorTransformer = ChordsEditorTransformer(history, textEditor, {
        ChordsNotation.mustParseById(state.chordsNotationId)
    })

    class LayoutState {
        var songTitle: String by mutableStateOf("")
        var artist: String by mutableStateOf("")
        var chordsNotationId: Long by mutableLongStateOf(ChordsNotation.default.id)
        var lyricsContent: MutableState<TextFieldValue> = mutableStateOf(TextFieldValue(text = ""))
        var artistAutocompleteExpanded = mutableStateOf(false)
        var chordsNotationExpanded = mutableStateOf(false)
        var artistAutocompleteOptions: MutableList<String> = mutableStateListOf()
        val contentFocusRequester: FocusRequester = FocusRequester()
        val horizontalScroll: ScrollState = ScrollState(0)
    }

    override fun showLayout(layout: View) {
        super.showLayout(layout)

        allCategoryNames = getAllCategoryNames()

        layout.findViewById<ImageButton>(R.id.goBackButton)?.setOnClickListener {
            onBackClicked()
        }

        layout.findViewById<ImageButton>(R.id.saveSongButton).setOnClickListener(SafeClickListener {
            saveSong()
        })

        layout.findViewById<ImageButton>(R.id.moreActionsButton)
            .setOnClickListener(SafeClickListener {
                showMoreActions()
            })

        val thisLayout = this
        layout.findViewById<ComposeView>(R.id.compose_view).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
            setContent {
                AppTheme {
                    MainComponent(thisLayout)
                }
            }
        }

        history.reset(textEditor)
    }

    fun openUrlChordFormat() {
        webviewLayoutController.openUrlChordFormat()
    }

    private fun getAllCategoryNames(): List<String> {
        val names = songsRepository.customSongsRepo.allCategoryNames.get() +
                songsRepository.allSongsRepo.publicCategories.get().mapNotNull { it.displayName }
        return names.distinct().sorted()
    }

    fun fillArtistAutocompleteOptions() {
        val artistAutocompleteOptions = when {
            state.artist.length < 2 -> emptyList()
            else -> allCategoryNames
                .filter { it.lowercase(StringSimplifier.locale).startsWith(state.artist.lowercase(
                    StringSimplifier.locale
                )) }
                .take(10)
        }
        state.artistAutocompleteOptions.clear()
        state.artistAutocompleteOptions.addAll(artistAutocompleteOptions)
        state.artistAutocompleteExpanded.value = artistAutocompleteOptions.isNotEmpty()
    }

    private fun showMoreActions() {
        val actions = mutableListOf(
            ContextMenuBuilder.Action(R.string.edit_song_save) {
                saveSong()
            },
            ContextMenuBuilder.Action(R.string.import_content_from_file) {
                importContentFromFile()
            },
            ContextMenuBuilder.Action(R.string.export_content_to_file) {
                exportContentToFile()
            },
            ContextMenuBuilder.Action(R.string.edit_song_remove) {
                removeSong()
            },
        )
        if (adminService.isAdminEnabled()) {
            actions += ContextMenuBuilder.Action(R.string.admin_create_category) {
                adminService.createCategoryDialog()
            }
        }
        contextMenuBuilder.showContextMenu(actions)
    }

    private fun importContentFromFile() {
        songImportFileChooser.showFileChooser()
    }

    private fun exportContentToFile() {
        val songTitle = state.songTitle
        val artist = state.artist
        val songContent = state.lyricsContent.value.text
        val notation = ChordsNotation.parseById(state.chordsNotationId) ?: preferencesState.chordsNotation
        customSongService.exportSongContent(songContent, songTitle, artist, notation)
    }

    fun setCurrentSong(song: Song?) {
        currentSong = song
        state.songTitle = song?.title ?: ""
        state.artist = song?.customCategoryName ?: ""
        state.chordsNotationId = song?.chordsNotation?.id ?: preferencesState.chordsNotation.id
        setLyricsContentText(song?.content ?: "")
        state.chordsNotationExpanded.value = false
        state.artistAutocompleteExpanded.value = false
    }

    private fun saveSong() {
        val songTitle = state.songTitle.ifBlank {
            return uiInfoService.showInfo(R.string.fill_in_all_fields)
        }
        val customCategoryName: String? = state.artist.ifBlank { null }
        val chordsNotation: ChordsNotation = ChordsNotation.parseById(state.chordsNotationId)
            ?: chordsNotationService.chordsNotation
        val content = state.lyricsContent.value.text
        val currentSong = currentSong
        if (currentSong == null) { // create
            this.currentSong = customSongService.addCustomSong(
                songTitle, customCategoryName, content, chordsNotation,
            )
        } else { // update
            customSongService.updateSong(
                currentSong, songTitle, customCategoryName, content, chordsNotation,
            )
        }
        uiInfoService.showInfoAction(R.string.edit_song_has_been_saved, actionResId = R.string.open_saved_song) {
            songOpener.openSongPreview(currentSong!!)
        }
        layoutController.showPreviousLayoutOrQuit()
    }

    private fun removeSong() {
        val songName = when (currentSong) {
            null -> state.songTitle
            else -> currentSong?.displayName() ?: ""
        }
        val message = uiInfoService.resString(R.string.confirm_remove_song, songName)
        ConfirmDialogBuilder().confirmAction(message) {
            if (currentSong == null) { // just cancel
                uiInfoService.showInfo(R.string.edit_song_has_been_removed)
            } else { // remove song from database
                customSongService.removeSong(currentSong!!)
            }
            layoutController.showPreviousLayoutOrQuit()
        }
    }

    override fun onBackClicked() {
        if (hasUnsavedChanges()) {
            uiInfoService.dialogThreeChoices(
                titleResId = R.string.confirm_unsaved_changes_title,
                messageResId = R.string.confirm_discard_custom_song_changes,
                positiveButton = R.string.confirm_unsaved_save,
                positiveAction = { saveSong() },
                negativeButton = R.string.confirm_discard_changes,
                negativeAction = { layoutController.showPreviousLayoutOrQuit() },
                neutralButton = R.string.action_cancel,
                neutralAction = {})
            return
        }

        val err = editorTransformer.quietValidate()
        if (err != null) {
            val message = uiResourceService.resString(R.string.editor_onexit_validation_failed, err)
            ConfirmDialogBuilder().confirmAction(message) {
                layoutController.showPreviousLayoutOrQuit()
            }
            return
        }

        layoutController.showPreviousLayoutOrQuit()
    }

    private fun hasUnsavedChanges(): Boolean {
        val songTitle = state.songTitle
        val customCategoryName = state.artist
        val songContent = state.lyricsContent.value.text
        val currentSong = currentSong
        if (currentSong == null) { // add
            if (songTitle.isNotEmpty()) return true
            if (customCategoryName.isNotEmpty()) return true
            if (songContent.isNotEmpty()) return true
        } else { // update
            if (currentSong.title != songTitle) return true
            if (currentSong.customCategoryName.orEmpty() != customCategoryName) return true
            if (currentSong.content.orEmpty() != songContent) return true
        }
        return false
    }

    fun setupImportedSong(title: String, artist: String?, content: String, notation: ChordsNotation?) {
        if (state.songTitle.isEmpty()) {
            state.songTitle = title
        }
        artist?.run {
            if (state.artist.isEmpty()) {
                state.artist = artist
            }
        }
        setLyricsContentText(content)
        notation?.let { notationN ->
            state.chordsNotationId = notationN.id
        }
    }

    fun showTransformMenu() {
        val actions = listOf(
            ContextMenuBuilder.Action(R.string.chords_editor_detect_and_move_chords_to_words) {
                wrapHistoryContext { editorTransformer.detectAndMoveChordsAboveToInline() }
            },
            ContextMenuBuilder.Action(R.string.chords_editor_move_chords_above_to_inline) {
                wrapHistoryContext { editorTransformer.moveChordsAboveToInline() }
            },
            ContextMenuBuilder.Action(R.string.chords_editor_move_chords_to_right) {
                wrapHistoryContext { editorTransformer.moveChordsAboveToRight() }
            },
            ContextMenuBuilder.Action(R.string.chords_editor_align_misplaced_chords) {
                wrapHistoryContext { editorTransformer.alignMisplacedChords() }
            },
            ContextMenuBuilder.Action(R.string.chords_editor_convert_from_notation) {
                wrapHistoryContext { editorTransformer.convertFromOtherNotationDialog() }
            },
            ContextMenuBuilder.Action(R.string.chords_editor_remove_double_empty_lines) {
                wrapHistoryContext { editorTransformer.removeDoubleEmptyLines() }
            },
            ContextMenuBuilder.Action(R.string.chords_editor_remove_bracket_content) {
                wrapHistoryContext { editorTransformer.removeBracketsContent() }
            },
            ContextMenuBuilder.Action(R.string.chords_editor_unmark_chords) {
                wrapHistoryContext { editorTransformer.unmarkChords() }
            },
            ContextMenuBuilder.Action(R.string.chords_editor_fis_to_sharp) {
                wrapHistoryContext { editorTransformer.chordsFisTofSharp() }
            },
            ContextMenuBuilder.Action(R.string.chords_editor_detect_chords_keeping_indent) {
                wrapHistoryContext { editorTransformer.detectChords(keepIndentation = false) }
            },
            ContextMenuBuilder.Action(R.string.chords_editor_transpose) {
                showTransposeMenu()
            },
        )
        contextMenuBuilder.showContextMenu(R.string.edit_song_transform_chords, actions)
    }

    private fun showTransposeMenu() {
        val actions = mutableListOf<ContextMenuBuilder.Action>()
        var label: String

        for (i in -11..-2) {
            label = uiInfoService.resString(R.string.chords_editor_x_semitones, i.toString())
            val action = ContextMenuBuilder.Action(label) {
                wrapHistoryContext { editorTransformer.transposeBy(i) }
            }
            actions.add(action)
        }
        label = uiInfoService.resString(R.string.chords_editor_x_semitone, "-1")
        actions.add(ContextMenuBuilder.Action(label) {
            wrapHistoryContext { editorTransformer.transposeBy(-1) }
        })
        label = uiInfoService.resString(R.string.chords_editor_x_semitone, "+1")
        actions.add(ContextMenuBuilder.Action(label) {
            wrapHistoryContext { editorTransformer.transposeBy(+1) }
        })
        for (i in 2..11) {
            label = uiInfoService.resString(R.string.chords_editor_x_semitones, "+$i")
            val action = ContextMenuBuilder.Action(label) {
                wrapHistoryContext { editorTransformer.transposeBy(i) }
            }
            actions.add(action)
        }

        contextMenuBuilder.showContextMenu(R.string.chords_editor_transpose_by_title, actions)
    }

    fun wrapHistoryContext(action: () -> Unit) {
        history.save(textEditor)
        action.invoke()
        history.restoreSelectionFromHistory(textEditor)
    }

    private fun setLyricsContentText(text: String) {
        state.lyricsContent.value = TextFieldValue(
            text = text,
            selection = TextRange(start = text.length, end = text.length), // cursor at the end
        )
    }

    private fun setLyricsSelection(start: Int, end: Int? = null) {
        val mEnd = end ?: start
        state.lyricsContent.value = state.lyricsContent.value
            .copy(selection = TextRange(start, mEnd))
    }

    fun onLyricsFieldChange(change: TextFieldValue) {
        val currentSelection = state.lyricsContent.value.selection
        // prevent from reversing selection for no reason
        if (change.selection.start != change.selection.end &&
            change.selection.start == currentSelection.end &&
            change.selection.end == currentSelection.start) {
            state.lyricsContent.value = change.copy(selection = TextRange(currentSelection.start, currentSelection.end))
        } else {
            state.lyricsContent.value = change
        }
    }

    private fun isSelecting(): Boolean {
        val selMin = state.lyricsContent.value.selection.min
        val selMax = state.lyricsContent.value.selection.max
        return selMin < selMax
    }

    fun quickCursorMove(direction: Int) {
        val text = state.lyricsContent.value.text
        val selStart = state.lyricsContent.value.selection.start
        val selEnd = state.lyricsContent.value.selection.end
        when (isSelecting()) {
            true -> { // expand selection
                var newSelEnd = selEnd
                if (direction == -1) {
                    newSelEnd--
                    if (newSelEnd < 0) newSelEnd = 0
                } else {
                    newSelEnd++
                    if (newSelEnd > text.length) newSelEnd = text.length
                }
                if (newSelEnd != selEnd) {
                    setLyricsSelection(selStart, newSelEnd)
                } else {
                    setLyricsSelection(newSelEnd)
                }
            }
            else -> { // no selection, move
                var cursor = selStart + direction
                if (cursor < 0) cursor = 0
                if (cursor > text.length) cursor = text.length
                setLyricsSelection(cursor)
            }
        }
    }

    fun undoChange() {
        if (history.isEmpty())
            return uiInfoService.showToast(R.string.no_undo_changes)
        history.revertLast(textEditor)
    }

    override fun onLayoutExit() {
        softKeyboardService.hideSoftKeyboard()
    }
}