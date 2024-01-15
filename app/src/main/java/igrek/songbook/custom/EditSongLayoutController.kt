package igrek.songbook.custom

import android.view.KeyEvent
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import igrek.songbook.R
import igrek.songbook.about.WebviewLayoutController
import igrek.songbook.admin.AdminService
import igrek.songbook.editor.ChordsEditorLayoutController
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.errorcheck.SafeClickListener
import igrek.songbook.info.errorcheck.UiErrorHandler
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.layout.GlobalFocusTraverser
import igrek.songbook.layout.InflatedLayout
import igrek.songbook.layout.contextmenu.ContextMenuBuilder
import igrek.songbook.layout.dialog.ConfirmDialogBuilder
import igrek.songbook.layout.spinner.ChordNotationSpinner
import igrek.songbook.persistence.general.model.Song
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.settings.chordsnotation.ChordsNotation
import igrek.songbook.settings.chordsnotation.ChordsNotationService
import igrek.songbook.settings.preferences.SettingsState
import igrek.songbook.songpreview.SongOpener
import igrek.songbook.system.SoftKeyboardService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable


class EditSongLayoutController(
    uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
    customSongService: LazyInject<CustomSongService> = appFactory.customSongService,
    softKeyboardService: LazyInject<SoftKeyboardService> = appFactory.softKeyboardService,
    songImportFileChooser: LazyInject<SongImportFileChooser> = appFactory.songImportFileChooser,
    chordsEditorLayoutController: LazyInject<ChordsEditorLayoutController> = appFactory.chordsEditorLayoutController,
    chordsNotationService: LazyInject<ChordsNotationService> = appFactory.chordsNotationService,
    contextMenuBuilder: LazyInject<ContextMenuBuilder> = appFactory.contextMenuBuilder,
    settingsState: LazyInject<SettingsState> = appFactory.settingsState,
    songsRepository: LazyInject<SongsRepository> = appFactory.songsRepository,
    adminService: LazyInject<AdminService> = appFactory.adminService,
    globalFocusTraverser: LazyInject<GlobalFocusTraverser> = appFactory.globalFocusTraverser,
    webviewLayoutController: LazyInject<WebviewLayoutController> = appFactory.webviewLayoutController,
    songOpener: LazyInject<SongOpener> = appFactory.songOpener,
) : InflatedLayout(
    _layoutResourceId = R.layout.screen_custom_song_details
) {
    private val uiInfoService by LazyExtractor(uiInfoService)
    private val customSongService by LazyExtractor(customSongService)
    private val softKeyboardService by LazyExtractor(softKeyboardService)
    private val songImportFileChooser by LazyExtractor(songImportFileChooser)
    private val chordsEditorLayoutController by LazyExtractor(chordsEditorLayoutController)
    private val chordsNotationService by LazyExtractor(chordsNotationService)
    private val contextMenuBuilder by LazyExtractor(contextMenuBuilder)
    private val preferencesState by LazyExtractor(settingsState)
    private val songsRepository by LazyExtractor(songsRepository)
    private val adminService by LazyExtractor(adminService)
    private val globalFocusTraverser by LazyExtractor(globalFocusTraverser)
    private val webviewLayoutController by LazyExtractor(webviewLayoutController)
    private val songOpener by LazyExtractor(songOpener)

    private var songTitleEdit: EditText? = null
    private var songContentEdit: EditText? = null
    private var customCategoryNameEdit: AppCompatAutoCompleteTextView? = null
    private var chordsNotationSpinner: ChordNotationSpinner? = null

    private var currentSong: Song? = null
    private var songTitle: String? = null
    private var songContent: String? = null
    private var customCategoryName: String? = null
    private var songChordsNotation: ChordsNotation? = null
    private var subscriptions = mutableListOf<Disposable>()

    override fun showLayout(layout: View) {
        super.showLayout(layout)

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

        layout.findViewById<ImageButton>(R.id.tooltipEditChordsLyricsInfo)?.let {
            it.setOnClickListener {
                webviewLayoutController.openUrlChordFormat()
            }
            globalFocusTraverser.setUpDownKeyListener(it)
        }

        songContentEdit = layout.findViewById<EditText>(R.id.songContentEdit)?.also {
            it.setText(songContent.orEmpty())
            it.setOnClickListener { openInChordsEditor() }
            it.setOnEditorActionListener { _, _, event: KeyEvent? ->
                when (event?.action) {
                    KeyEvent.ACTION_DOWN -> {
                        openInChordsEditor()
                        true
                    }
                    else -> false
                }
            }
            globalFocusTraverser.setUpDownKeyListener(it)
        }

        songTitleEdit = layout.findViewById<EditText>(R.id.songTitleEdit)?.also {
            it.setText(songTitle.orEmpty())
            globalFocusTraverser.setUpDownKeyListener(it)
        }

        customCategoryNameEdit =
            layout.findViewById<AppCompatAutoCompleteTextView>(R.id.customCategoryNameEdit)?.apply {
                setText(customCategoryName.orEmpty())
                threshold = 1
                globalFocusTraverser.setUpDownKeyListener(this)
            }
        updateCategoryAutocompleter()

        chordsNotationSpinner = ChordNotationSpinner(
            spinnerId = R.id.songChordNotationSpinner,
            layout = layout,
            activity = activity,
            chordsNotationDisplayNames = chordsNotationService.chordsNotationDisplayNames
        ).also {
            it.selectedNotation = songChordsNotation ?: preferencesState.chordsNotation
            globalFocusTraverser.setUpDownKeyListener(it.spinner)
        }

        subscriptions.forEach { s -> s.dispose() }
        subscriptions.clear()
        subscriptions.add(songsRepository.dbChangeSubject
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (isLayoutVisible())
                    updateCategoryAutocompleter()
            }, UiErrorHandler::handleError)
        )
    }

    private fun updateCategoryAutocompleter() {
        val categoryNames = getAllCategoryNames().toTypedArray()
        val adapter =
            ArrayAdapter(activity, android.R.layout.simple_dropdown_item_1line, categoryNames)
        customCategoryNameEdit?.setAdapter(adapter)
    }

    private fun getAllCategoryNames(): List<String> {
        return songsRepository.customSongsRepo.allCategoryNames.get() +
            songsRepository.allSongsRepo.publicCategories.get()
            .mapNotNull { it.displayName }
            .sorted()
    }

    private fun showMoreActions() {
        val actions = mutableListOf(
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
            },
        )
        if (adminService.isAdminEnabled()) {
            actions += ContextMenuBuilder.Action(R.string.admin_create_category) {
                adminService.createCategoryDialog()
            }
        }
        contextMenuBuilder.showContextMenu(actions)
    }

    private fun openInChordsEditor() {
        this.songTitle = songTitleEdit?.text?.toString().orEmpty()
        this.songContent = songContentEdit?.text?.toString().orEmpty()
        this.customCategoryName = customCategoryNameEdit?.text?.toString().orEmpty()
        val chordsNotation =
            chordsNotationSpinner?.selectedNotation ?: preferencesState.chordsNotation
        this.songChordsNotation = chordsNotation

        chordsEditorLayoutController.chordsNotation = chordsNotation
        chordsEditorLayoutController.loadContent = songContentEdit?.text.toString()
        layoutController.showLayout(ChordsEditorLayoutController::class)
    }

    private fun importContentFromFile() {
        songImportFileChooser.showFileChooser()
    }

    private fun exportContentToFile() {
        val songTitle = songTitleEdit?.text?.toString() ?: "Untitled"
        val artist = customCategoryNameEdit?.text?.toString().orEmpty()
        val songContent = songContentEdit?.text?.toString().orEmpty()
        val notation = chordsNotationSpinner?.selectedNotation ?: preferencesState.chordsNotation
        customSongService.exportSongContent(songContent, songTitle, artist, notation)
    }

    fun setCurrentSong(song: Song?) {
        this.currentSong = song
        this.songTitle = song?.title
        this.songContent = song?.content
        this.customCategoryName = song?.customCategoryName
        this.songChordsNotation = song?.chordsNotation
    }

    fun setSongContent(content: String) {
        this.songContent = content
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
            currentSong = customSongService.addCustomSong(
                songTitle,
                customCategoryName,
                songContent,
                chordsNotation,
            )
        } else {
            // update
            customSongService.updateSong(
                currentSong!!,
                songTitle,
                customCategoryName,
                songContent,
                chordsNotation,
            )
        }

        uiInfoService.showInfoAction(R.string.edit_song_has_been_saved, actionResId = R.string.open_saved_song) {
            songOpener.openSongPreview(currentSong!!)
        }
        layoutController.showPreviousLayoutOrQuit()
    }

    private fun removeSong() {
        val songName = when (currentSong) {
            null -> songTitle
            else -> currentSong?.displayName() ?: ""
        }
        val message = uiInfoService.resString(R.string.confirm_remove_song, songName)
        ConfirmDialogBuilder().confirmAction(message) {
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
            uiInfoService.dialogThreeChoices(
                titleResId = R.string.confirm_unsaved_changes_title,
                messageResId = R.string.confirm_discard_custom_song_changes,
                positiveButton = R.string.confirm_unsaved_save,
                positiveAction = { saveSong() },
                negativeButton = R.string.confirm_discard_changes,
                negativeAction = { layoutController.showPreviousLayoutOrQuit() },
                neutralButton = R.string.action_cancel,
                neutralAction = {})
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

    fun setupImportedSong(title: String, artist: String?, content: String, notation: ChordsNotation?) {
        if (songTitleEdit?.text?.toString().isNullOrEmpty()) {
            songTitleEdit?.setText(title)
        }
        artist?.run {
            if (customCategoryNameEdit?.text?.toString().isNullOrEmpty()) {
                customCategoryNameEdit?.setText(artist)
            }
        }
        songContentEdit?.setText(content)
        notation?.let { notationN ->
            chordsNotationSpinner?.selectedNotation = notationN
        }
    }
}
