package igrek.songbook.custom.editor

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import dagger.Lazy
import igrek.songbook.R
import igrek.songbook.custom.EditSongLayoutController
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.UiResourceService
import igrek.songbook.info.errorcheck.SafeClickListener
import igrek.songbook.layout.LayoutController
import igrek.songbook.layout.MainLayout
import igrek.songbook.layout.contextmenu.ContextMenuBuilder
import igrek.songbook.layout.dialog.ConfirmDialogBuilder
import igrek.songbook.layout.navigation.NavigationMenuController
import igrek.songbook.settings.chordsnotation.ChordsNotation
import igrek.songbook.system.SoftKeyboardService
import javax.inject.Inject


class ChordsEditorLayoutController : MainLayout {

    @Inject
    lateinit var layoutController: LayoutController
    @Inject
    lateinit var uiInfoService: UiInfoService
    @Inject
    lateinit var uiResourceService: UiResourceService
    @Inject
    lateinit var activity: AppCompatActivity
    @Inject
    lateinit var navigationMenuController: NavigationMenuController
    @Inject
    lateinit var editSongLayoutController: Lazy<EditSongLayoutController>
    @Inject
    lateinit var softKeyboardService: SoftKeyboardService
    @Inject
    lateinit var contextMenuBuilder: ContextMenuBuilder

    private var contentEdit: EditText? = null
    private var layout: View? = null
    private var chordsNotation: ChordsNotation? = null
    private var history = LyricsEditorHistory()
    private var transformer: ChordsEditorTransformer? = null

    init {
        DaggerIoc.factoryComponent.inject(this)
    }

    override fun getLayoutResourceId(): Int {
        return R.layout.screen_chords_editor
    }

    override fun showLayout(layout: View) {
        this.layout = layout

        // Toolbar
        val toolbar1 = layout.findViewById<Toolbar>(R.id.toolbar1)
        activity.setSupportActionBar(toolbar1)
        val actionBar = activity.supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false)
            actionBar.setDisplayShowHomeEnabled(false)
        }
        // navigation menu button
        val navMenuButton = layout.findViewById<ImageButton>(R.id.navMenuButton)
        navMenuButton.setOnClickListener { navigationMenuController.navDrawerShow() }

        val goBackButton = layout.findViewById<ImageButton>(R.id.goBackButton)
        goBackButton.setOnClickListener(SafeClickListener {
            onBackClicked()
        })

        val tooltipEditChordsLyricsInfo = layout.findViewById<ImageButton>(R.id.tooltipEditChordsLyricsInfo)
        tooltipEditChordsLyricsInfo.setOnClickListener {
            uiInfoService.showTooltip(R.string.tooltip_edit_chords_lyrics)
        }

        contentEdit = layout.findViewById(R.id.songContentEdit)
        contentEdit?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                if (start == 0 && count == s?.length) {
                    return // skip in order not to save undo / transforming operations again
                }
                history.save(contentEdit!!)
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
        softKeyboardService.showSoftKeyboard(contentEdit)

        transformer = ChordsEditorTransformer(contentEdit!!, history, chordsNotation, uiResourceService, uiInfoService)

        buttonOnClick(R.id.addChordButton) { transformer?.onWrapChordClick() }
        buttonOnClick(R.id.addChordSplitterButton) { transformer?.addChordSplitter() }
        buttonOnClick(R.id.copyChordButton) { transformer?.onCopyChordClick() }
        buttonOnClick(R.id.pasteChordButton) { transformer?.onPasteChordClick() }
        buttonOnClick(R.id.detectChordsButton) { wrapHistoryContext { transformer?.detectChords() } }
        buttonOnClick(R.id.undoChordsButton) { undoChange() }
        buttonOnClick(R.id.transformChordsButton) { showTransformMenu() }
        buttonOnClick(R.id.moveLeftButton) { moveCursor(-1) }
        buttonOnClick(R.id.moveRightButton) { moveCursor(+1) }
        buttonOnClick(R.id.validateChordsButton) { transformer?.validateChords() }
        buttonOnClick(R.id.reformatTrimButton) { wrapHistoryContext { transformer?.reformatAndTrim() } }
    }

    private fun buttonOnClick(@IdRes buttonId: Int, onclickAction: () -> Unit) {
        val chordsNotationButton = layout?.findViewById<Button>(buttonId)
        chordsNotationButton?.setOnClickListener(SafeClickListener {
            onclickAction()
        })
    }

    private fun showTransformMenu() {
        val actions = listOf(
                ContextMenuBuilder.Action(R.string.chords_editor_move_chords_above_to_inline) {
                    wrapHistoryContext { transformer?.moveChordsAboveToInline() }
                },
                ContextMenuBuilder.Action(R.string.chords_editor_move_chords_to_right) {
                    wrapHistoryContext { transformer?.moveChordsAboveToRight() }
                },
                ContextMenuBuilder.Action(R.string.chords_editor_convert_from_notation) {
                    wrapHistoryContext { transformer?.convertFromOtherNotationDialog() }
                },
                ContextMenuBuilder.Action(R.string.chords_editor_remove_double_empty_lines) {
                    wrapHistoryContext { transformer?.removeDoubleEmptyLines() }
                },
                ContextMenuBuilder.Action(R.string.chords_editor_fis_to_sharp) {
                    wrapHistoryContext { transformer?.chordsFisTofSharp() }
                },
        )
        contextMenuBuilder.showContextMenu(R.string.edit_song_transform_chords, actions)
    }

    private fun wrapHistoryContext(action: () -> Unit) {
        history.save(contentEdit!!)
        action.invoke()
        restoreSelectionFromHistory()
    }

    private fun restoreSelectionFromHistory() {
        val lastSelection = history.peekLastSelection()
        if (lastSelection != null) {
            var selStart = lastSelection.first
            var selEnd = lastSelection.second
            val maxLength = contentEdit!!.text.length
            if (selStart > maxLength)
                selStart = maxLength
            if (selEnd > maxLength)
                selEnd = maxLength
            contentEdit?.setSelection(selStart, selEnd)
            contentEdit?.requestFocus()
        }
    }

    private fun moveCursor(delta: Int) {
        val edited = contentEdit!!.text.toString()
        var selStart = contentEdit!!.selectionStart

        selStart += delta
        if (selStart < 0)
            selStart = 0
        if (selStart > edited.length)
            selStart = edited.length

        contentEdit!!.setSelection(selStart, selStart)
        contentEdit!!.requestFocus()
    }

    private fun undoChange() {
        if (history.isEmpty()) {
            uiInfoService.showToast(R.string.no_undo_changes)
            return
        }
        history.revertLast(contentEdit!!)
    }

    override fun onBackClicked() {
        val err = transformer?.quietValidate()
        if (err != null) {
            val message = uiResourceService.resString(R.string.editor_onexit_validation_failed, err)
            ConfirmDialogBuilder().confirmAction(message) {
                returnNewContent()
            }
            return
        }
        returnNewContent()
    }

    fun setContent(content: String, chordsNotation: ChordsNotation?) {
        this.chordsNotation = chordsNotation
        transformer?.setContentWithSelection(content, 0, 0)
        history.reset(contentEdit!!)
        softKeyboardService.showSoftKeyboard(contentEdit)
        contentEdit?.setSelection(0, 0)
    }

    private fun returnNewContent() {
        val content = contentEdit?.text?.toString().orEmpty()
        layoutController.showPreviousLayoutOrQuit()
        editSongLayoutController.get().setSongContent(content)
    }

    override fun onLayoutExit() {
        softKeyboardService.hideSoftKeyboard()
    }
}
