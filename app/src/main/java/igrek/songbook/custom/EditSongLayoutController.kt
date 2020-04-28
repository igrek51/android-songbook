package igrek.songbook.custom

import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import igrek.songbook.R
import igrek.songbook.editor.ChordsEditorLayoutController
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.errorcheck.SafeClickListener
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.layout.LayoutController
import igrek.songbook.layout.MainLayout
import igrek.songbook.layout.contextmenu.ContextMenuBuilder
import igrek.songbook.layout.dialog.ConfirmDialogBuilder
import igrek.songbook.layout.navigation.NavigationMenuController
import igrek.songbook.layout.spinner.ChordNotationSpinner
import igrek.songbook.persistence.general.model.Song
import igrek.songbook.settings.chordsnotation.ChordsNotation
import igrek.songbook.settings.chordsnotation.ChordsNotationService
import igrek.songbook.settings.preferences.PreferencesState
import igrek.songbook.system.SoftKeyboardService


class EditSongLayoutController(
        layoutController: LazyInject<LayoutController> = appFactory.layoutController,
        uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
        appCompatActivity: LazyInject<AppCompatActivity> = appFactory.appCompatActivity,
        navigationMenuController: LazyInject<NavigationMenuController> = appFactory.navigationMenuController,
        customSongService: LazyInject<CustomSongService> = appFactory.customSongService,
        softKeyboardService: LazyInject<SoftKeyboardService> = appFactory.softKeyboardService,
        songImportFileChooser: LazyInject<SongImportFileChooser> = appFactory.songImportFileChooser,
        songExportFileChooser: LazyInject<SongExportFileChooser> = appFactory.songExportFileChooser,
        chordsEditorLayoutController: LazyInject<ChordsEditorLayoutController> = appFactory.chordsEditorLayoutController,
        chordsNotationService: LazyInject<ChordsNotationService> = appFactory.chordsNotationService,
        contextMenuBuilder: LazyInject<ContextMenuBuilder> = appFactory.contextMenuBuilder,
        preferencesState: LazyInject<PreferencesState> = appFactory.preferencesState,
) : MainLayout {
    private val layoutController by LazyExtractor(layoutController)
    private val uiInfoService by LazyExtractor(uiInfoService)
    private val activity by LazyExtractor(appCompatActivity)
    private val navigationMenuController by LazyExtractor(navigationMenuController)
    private val customSongService by LazyExtractor(customSongService)
    private val softKeyboardService by LazyExtractor(softKeyboardService)
    private val songImportFileChooser by LazyExtractor(songImportFileChooser)
    private val songExportFileChooser by LazyExtractor(songExportFileChooser)
    private val chordsEditorLayoutController by LazyExtractor(chordsEditorLayoutController)
    private val chordsNotationService by LazyExtractor(chordsNotationService)
    private val contextMenuBuilder by LazyExtractor(contextMenuBuilder)
    private val preferencesState by LazyExtractor(preferencesState)

    private var currentSong: Song? = null

    private var songTitleEdit: EditText? = null
    private var songContentEdit: EditText? = null
    private var customCategoryNameEdit: EditText? = null
    private var chordsNotationSpinner: ChordNotationSpinner? = null

    private var songTitle: String? = null
    private var songContent: String? = null
    private var customCategoryName: String? = null
    private var songChordsNotation: ChordsNotation? = null

    override fun getLayoutResourceId(): Int {
        return R.layout.screen_custom_song_details
    }

    override fun showLayout(layout: View) {
        val toolbar1 = layout.findViewById<Toolbar>(R.id.toolbar1)
        activity.setSupportActionBar(toolbar1)
        activity.supportActionBar?.run {
            this.setDisplayHomeAsUpEnabled(false)
            this.setDisplayShowHomeEnabled(false)
        }
        layout.findViewById<ImageButton>(R.id.navMenuButton).setOnClickListener {
            navigationMenuController.navDrawerShow()
        }

        layout.findViewById<ImageButton>(R.id.goBackButton)?.setOnClickListener {
            onBackClicked()
        }

        layout.findViewById<ImageButton>(R.id.saveSongButton).setOnClickListener(SafeClickListener {
            saveSong()
        })

        layout.findViewById<ImageButton>(R.id.moreActionsButton).setOnClickListener(SafeClickListener {
            showMoreActions()
        })

        layout.findViewById<ImageButton>(R.id.tooltipEditChordsLyricsInfo).setOnClickListener {
            uiInfoService.showTooltip(R.string.chords_editor_hint)
        }

        songContentEdit = layout.findViewById<EditText>(R.id.songContentEdit)?.also {
            it.setText(songContent.orEmpty())
            it.setOnClickListener { openInChordsEditor() }
        }

        songTitleEdit = layout.findViewById<EditText>(R.id.songTitleEdit)?.also {
            it.setText(songTitle.orEmpty())
        }

        customCategoryNameEdit = layout.findViewById<EditText>(R.id.customCategoryNameEdit)?.also {
            it.setText(customCategoryName.orEmpty())
        }

        chordsNotationSpinner = ChordNotationSpinner(
                spinnerId = R.id.songChordNotationSpinner,
                layout = layout,
                activity = activity,
                chordsNotationDisplayNames = chordsNotationService.chordsNotationDisplayNames
        ).also {
            it.selectedNotation = songChordsNotation ?: preferencesState.chordsNotation
        }
    }

    private fun showMoreActions() {
        contextMenuBuilder.showContextMenu(listOf(
                ContextMenuBuilder.Action(R.string.edit_song_open_in_editor) {
                    openInChordsEditor()
                },
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
                }
        ))
    }

    private fun openInChordsEditor() {
        this.songTitle = songTitleEdit?.text?.toString().orEmpty()
        this.songContent = songContentEdit?.text?.toString().orEmpty()
        this.customCategoryName = customCategoryNameEdit?.text?.toString().orEmpty()
        this.songChordsNotation = chordsNotationSpinner?.selectedNotation
                ?: chordsNotationService.chordsNotation

        layoutController.showLayout(ChordsEditorLayoutController::class)
        chordsEditorLayoutController.setContent(songContentEdit?.text.toString(), this.songChordsNotation)

        customSongService.showEditorHintsIfNeeded()
    }

    private fun importContentFromFile() {
        songImportFileChooser.showFileChooser()
    }

    private fun exportContentToFile() {
        var songTitle = songTitleEdit?.text?.toString().orEmpty()
        songTitle = songTitle.takeIf { it.toLowerCase().endsWith(".txt") } ?: "$songTitle.txt"
        val songContent = songContentEdit?.text?.toString().orEmpty()
        songExportFileChooser.showFileChooser(songContent, songTitle) {
            uiInfoService.showInfo(R.string.song_content_exported)
        }
    }

    fun setCurrentSong(song: Song?) {
        this.currentSong = song
        this.songTitle = song?.title
        this.songContent = song?.content
        this.customCategoryName = song?.customCategoryName
        this.songChordsNotation = song?.chordsNotation
    }

    fun setSongContent(content: String) {
        songContentEdit?.setText(content)
    }

    private fun saveSong() {
        val songTitle = songTitleEdit?.text.toString()
        if (songTitle.isEmpty()) {
            uiInfoService.showInfo(R.string.fill_in_all_fields)
            return
        }
        val songContent = songContentEdit?.text.toString()
        val customCategoryName: String? = customCategoryNameEdit?.text.toString().ifEmpty { null }
        val chordsNotation: ChordsNotation = chordsNotationSpinner?.selectedNotation
                ?: chordsNotationService.chordsNotation

        if (currentSong == null) {
            // add
            currentSong = customSongService.addCustomSong(songTitle, customCategoryName, songContent, chordsNotation)
        } else {
            // update
            customSongService.updateSong(currentSong!!, songTitle, customCategoryName, songContent, chordsNotation)
        }

        uiInfoService.showInfo(R.string.edit_song_has_been_saved)
        layoutController.showPreviousLayoutOrQuit()
    }

    private fun removeSong() {
        ConfirmDialogBuilder().confirmAction(R.string.confirm_remove_song) {
            if (currentSong == null) {
                // just cancel
                uiInfoService.showInfo(R.string.edit_song_has_been_removed)
            } else {
                // remove song from database
                customSongService.removeSong(currentSong!!)
            }
            layoutController.showPreviousLayoutOrQuit()
        }
    }

    override fun onBackClicked() {
        if (hasUnsavedChanges()) {
            ConfirmDialogBuilder().chooseFromThree(
                    messageId = R.string.confirm_discard_custom_song_changes,
                    titleResId = R.string.confirm_unsaved_changes_title,
                    positiveButton = R.string.confirm_unsaved_save, positiveAction = { saveSong() },
                    negativeButton = R.string.confirm_discard_changes, negativeAction = { layoutController.showPreviousLayoutOrQuit() },
                    neutralButton = R.string.action_cancel, neutralAction = {})
        } else {
            layoutController.showPreviousLayoutOrQuit()
        }
    }

    private fun hasUnsavedChanges(): Boolean {
        val songTitle = songTitleEdit?.text?.toString().orEmpty()
        val customCategoryName = customCategoryNameEdit?.text?.toString().orEmpty()
        val songContent = songContentEdit?.text?.toString().orEmpty()
        if (currentSong == null) { // add
            if (songTitle.isNotEmpty()) return true
            if (customCategoryName.isNotEmpty()) return true
            if (songContent.isNotEmpty()) return true
        } else { // update
            if (currentSong?.title.orEmpty() != songTitle) return true
            if (currentSong?.customCategoryName.orEmpty() != customCategoryName) return true
            if (currentSong?.content.orEmpty() != songContent) return true
        }
        return false
    }

    override fun onLayoutExit() {
        softKeyboardService.hideSoftKeyboard()
    }

    fun setupImportedSong(filename: String, content: String) {
        if (songTitleEdit?.text.toString().isEmpty()) {
            songTitleEdit?.setText(filename)
        }
        songContentEdit?.setText(content)
    }
}
