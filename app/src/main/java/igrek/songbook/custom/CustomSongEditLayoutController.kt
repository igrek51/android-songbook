package igrek.songbook.custom

import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import dagger.Lazy
import igrek.songbook.R
import igrek.songbook.custom.editor.ChordsEditorLayoutController
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.errorcheck.SafeClickListener
import igrek.songbook.layout.LayoutController
import igrek.songbook.layout.LayoutState
import igrek.songbook.layout.MainLayout
import igrek.songbook.layout.contextmenu.ContextMenuBuilder
import igrek.songbook.layout.dialog.ConfirmDialogBuilder
import igrek.songbook.layout.navigation.NavigationMenuController
import igrek.songbook.persistence.general.model.Song
import igrek.songbook.settings.chordsnotation.ChordsNotationService
import igrek.songbook.system.SoftKeyboardService
import javax.inject.Inject

class CustomSongEditLayoutController : MainLayout {

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

    private var currentSong: Song? = null
    private var songTitle: String? = null
    private var songContent: String? = null
    private var customCategoryName: String? = null

    private var songTitleEdit: EditText? = null
    private var songContentEdit: EditText? = null
    private var customCategoryNameEdit: EditText? = null

    init {
        DaggerIoc.factoryComponent.inject(this)
    }

    override fun showLayout(layout: View) {
        val toolbar1 = layout.findViewById<Toolbar>(R.id.toolbar1)
        activity.setSupportActionBar(toolbar1)
        val actionBar = activity.supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false)
            actionBar.setDisplayShowHomeEnabled(false)
        }
        val navMenuButton = layout.findViewById<ImageButton>(R.id.navMenuButton)
        navMenuButton.setOnClickListener { navigationMenuController.navDrawerShow() }

        val saveSongButton = layout.findViewById<ImageButton>(R.id.saveSongButton)
        saveSongButton.setOnClickListener(SafeClickListener {
            saveSong()
        })

        val moreActionsButton = layout.findViewById<ImageButton>(R.id.moreActionsButton)
        moreActionsButton.setOnClickListener(SafeClickListener {
            showMoreActions()
        })


        val tooltipEditChordsLyricsInfo = layout.findViewById<ImageButton>(R.id.tooltipEditChordsLyricsInfo)
        tooltipEditChordsLyricsInfo.setOnClickListener {
            uiInfoService.showTooltip(R.string.tooltip_edit_chords_lyrics)
        }

        songContentEdit = layout.findViewById(R.id.songContentEdit)
        songContentEdit!!.setText(songContent)
        songContentEdit!!.setOnClickListener { openInChordsEditor() }

        songTitleEdit = layout.findViewById(R.id.songTitleEdit)
        songTitleEdit!!.setText(songTitle)

        customCategoryNameEdit = layout.findViewById(R.id.customCategoryNameEdit)
        customCategoryNameEdit!!.setText(customCategoryName)
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
        songTitle = songTitleEdit!!.text.toString()
        songContent = songContentEdit!!.text.toString()
        customCategoryName = customCategoryNameEdit!!.text.toString()
        layoutController.showSongChordEditor()
        val chordsNotation = chordsNotationService.chordsNotation
        chordsEditorLayoutController.get().setContent(songContentEdit?.text.toString(), chordsNotation)
    }

    private fun importContentFromFile() {
        songImportFileChooser.get().showFileChooser()
    }

    fun setCurrentSong(song: Song?) {
        this.currentSong = song
        if (currentSong == null) {
            songTitle = null
            songContent = null
            customCategoryName = null
        } else {
            songTitle = currentSong!!.title
            songContent = currentSong!!.content
            customCategoryName = currentSong!!.customCategoryName
        }
    }

    fun setSongContent(content: String) {
        songContentEdit!!.setText(content)
    }

    private fun saveSong() {
        songTitle = songTitleEdit!!.text.toString()
        songContent = songContentEdit!!.text.toString()
        customCategoryName = customCategoryNameEdit!!.text.toString()

        if (songTitle!!.isEmpty()) {
            uiInfoService.showInfo(R.string.fill_in_all_fields)
            return
        }

        if (customCategoryName!!.isEmpty())
            customCategoryName = null

        if (currentSong == null) {
            // add
            currentSong = customSongService.get()
                    .addCustomSong(songTitle!!, customCategoryName, songContent ?: "")
        } else {
            // update
            customSongService.get()
                    .updateSong(currentSong!!, songTitle!!, customCategoryName, songContent)
        }
        uiInfoService.showInfo(R.string.edit_song_has_been_saved)
        layoutController.showCustomSongs()
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
            layoutController.showCustomSongs()
        }
    }

    override fun getLayoutState(): LayoutState {
        return LayoutState.CUSTOM_SONG_EDIT
    }

    override fun getLayoutResourceId(): Int {
        return R.layout.screen_custom_song_details
    }

    override fun onBackClicked() {
        if (hasUnsavedChanges()) {
            ConfirmDialogBuilder().confirmAction(R.string.confirm_discard_custom_song_changes) {
                layoutController.showCustomSongs()
            }
        } else {
            layoutController.showCustomSongs()
        }
    }

    private fun hasUnsavedChanges(): Boolean {
        songTitle = songTitleEdit!!.text.toString()
        customCategoryName = customCategoryNameEdit!!.text.toString()
        songContent = songContentEdit!!.text.toString()
        if (currentSong == null) { // add
            if (songTitle!!.isNotEmpty()) return true
            if (customCategoryName!!.isNotEmpty()) return true
            if (songContent!!.isNotEmpty()) return true
        } else { // update
            if (currentSong?.title != songTitle) return true
            if (currentSong?.customCategoryName != customCategoryName) return true
            if (currentSong?.content != songContent) return true
        }
        return false
    }

    override fun onLayoutExit() {
        softKeyboardService.hideSoftKeyboard()
    }

    fun setSongFromFile(filename: String, content: String) {
        songTitle = songTitleEdit!!.text.toString()
        songContent = songContentEdit!!.text.toString()
        customCategoryName = customCategoryNameEdit!!.text.toString()

        if (songTitle!!.isEmpty()) {
            songTitle = filename
            songTitleEdit!!.setText(songTitle)
        }

        songContent = content
        songContentEdit!!.setText(songContent)
    }
}
