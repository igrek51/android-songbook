package igrek.songbook.custom

import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import dagger.Lazy
import igrek.songbook.R
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.editor.ChordsEditorLayoutController
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.errorcheck.SafeClickListener
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
import javax.inject.Inject


class EditSongLayoutController : MainLayout {

    @Inject
    lateinit var layoutController: LayoutController
    @Inject
    lateinit var uiInfoService: UiInfoService
    @Inject
    lateinit var activity: AppCompatActivity
    @Inject
    lateinit var navigationMenuController: NavigationMenuController
    @Inject
    lateinit var customSongService: Lazy<CustomSongService>
    @Inject
    lateinit var softKeyboardService: SoftKeyboardService
    @Inject
    lateinit var songImportFileChooser: Lazy<SongImportFileChooser>
    @Inject
    lateinit var chordsEditorLayoutController: Lazy<ChordsEditorLayoutController>
    @Inject
    lateinit var chordsNotationService: ChordsNotationService
    @Inject
    lateinit var contextMenuBuilder: ContextMenuBuilder
    @Inject
    lateinit var preferencesState: PreferencesState

    private var currentSong: Song? = null

    private var songTitleEdit: EditText? = null
    private var songContentEdit: EditText? = null
    private var customCategoryNameEdit: EditText? = null
    private var chordsNotationSpinner: ChordNotationSpinner? = null

    private var songTitle: String? = null
    private var songContent: String? = null
    private var customCategoryName: String? = null
    private var songChordsNotation: ChordsNotation? = null

    init {
        DaggerIoc.factoryComponent.inject(this)
    }

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

        layout.findViewById<ImageButton>(R.id.saveSongButton).setOnClickListener(SafeClickListener {
            saveSong()
        })

        layout.findViewById<ImageButton>(R.id.moreActionsButton).setOnClickListener(SafeClickListener {
            showMoreActions()
        })

        layout.findViewById<ImageButton>(R.id.tooltipEditChordsLyricsInfo).setOnClickListener {
            uiInfoService.showTooltip(R.string.tooltip_edit_chords_lyrics)
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
        chordsEditorLayoutController.get().setContent(songContentEdit?.text.toString(), this.songChordsNotation)
    }

    private fun importContentFromFile() {
        songImportFileChooser.get().showFileChooser()
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
            currentSong = customSongService.get()
                    .addCustomSong(songTitle, customCategoryName, songContent, chordsNotation)
        } else {
            // update
            customSongService.get()
                    .updateSong(currentSong!!, songTitle, customCategoryName, songContent, chordsNotation)
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
                customSongService.get().removeSong(currentSong!!)
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
