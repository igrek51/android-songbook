package igrek.songbook.custom

import android.support.annotation.IdRes
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import dagger.Lazy
import igrek.songbook.R
import igrek.songbook.chords.ChordsConverter
import igrek.songbook.chords.detector.ChordsDetector
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.UiResourceService
import igrek.songbook.info.errorcheck.SafeClickListener
import igrek.songbook.layout.LayoutController
import igrek.songbook.layout.LayoutState
import igrek.songbook.layout.MainLayout
import igrek.songbook.layout.contextmenu.ContextMenuBuilder
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
    lateinit var customSongEditLayoutController: Lazy<CustomSongEditLayoutController>
    @Inject
    lateinit var softKeyboardService: SoftKeyboardService
    @Inject
    lateinit var contextMenuBuilder: ContextMenuBuilder

    private var contentEdit: EditText? = null
    private var clipboardChords: String? = null
    private var layout: View? = null
    private var chordsNotation: ChordsNotation? = null
    private var history = LyricsEditorHistory()

    init {
        DaggerIoc.getFactoryComponent().inject(this)
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

        buttonOnClick(R.id.addChordButton) { onAddChordClick() }
        buttonOnClick(R.id.addChordSplitterButton) { addChordSplitter() }
        buttonOnClick(R.id.copyChordButton) { onCopyChordClick() }
        buttonOnClick(R.id.pasteChordButton) { onPasteChordClick() }
        buttonOnClick(R.id.detectChordsButton) { wrapHistoryContext { detectChords() } }
        buttonOnClick(R.id.undoChordsButton) { undoChange() }
        buttonOnClick(R.id.transformChordsButton) { showTransformMenu() }
        buttonOnClick(R.id.chordsNotationButton) { chooseChordsNotation() }
        buttonOnClick(R.id.moveLeftButton) { moveCursor(-1) }
        buttonOnClick(R.id.moveRightButton) { moveCursor(+1) }
        buttonOnClick(R.id.validateChordsButton) { validateChords() }

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
    }

    private fun buttonOnClick(@IdRes buttonId: Int, onclickAction: () -> Unit) {
        val chordsNotationButton = layout?.findViewById<Button>(buttonId)
        chordsNotationButton?.setOnClickListener(SafeClickListener {
            onclickAction()
        })
    }

    private fun showTransformMenu() {
        val actions = listOf(
                ContextMenuBuilder.Action(R.string.chords_editor_reformat_trim) {
                    wrapHistoryContext { reformatAndTrim() }
                },
                ContextMenuBuilder.Action(R.string.chords_editor_move_chords_to_right) {
                    wrapHistoryContext { moveChordsAboveToRight() }
                },
                ContextMenuBuilder.Action(R.string.chords_editor_fis_to_sharp) {
                    wrapHistoryContext { chordsFisTofSharp() }
                }
        )
        contextMenuBuilder.showContextMenu(R.string.edit_song_transform_chords, actions)
    }

    private fun wrapHistoryContext(action: () -> Unit) {
        history.save(contentEdit!!)
        action.invoke()
        restoreSelectionFromHistory()
    }

    private fun validateChords() {
        val text = contentEdit!!.text.toString()
        try {
            validateChordsBrackets(text)
            validateChordsNotation(text)
            uiInfoService.showToast(R.string.chords_are_valid)
        } catch (e: ChordsValidationError) {
            val placeholder = uiResourceService.resString(R.string.chords_invalid)
            var errorMessage = e.errorMessage
            if (errorMessage.isNullOrEmpty())
                errorMessage = uiResourceService.resString(e.messageResId!!)
            uiInfoService.showToast(placeholder.format(errorMessage))
        }
    }

    private fun validateChordsBrackets(text: String) {
        var inBracket = false
        for (char in text) {
            when (char) {
                '[' -> {
                    if (inBracket)
                        throw ChordsValidationError(R.string.chords_invalid_missing_closing_bracket)
                    inBracket = true
                }
                ']' -> {
                    if (!inBracket)
                        throw ChordsValidationError(R.string.chords_invalid_missing_opening_bracket)
                    inBracket = false
                }
            }
        }
        if (inBracket)
            throw ChordsValidationError(R.string.chords_invalid_missing_closing_bracket)
    }

    private fun validateChordsNotation(text: String) {
        val detector = ChordsDetector(chordsNotation)
        text.replace(Regex("""\[(.*?)]""")) { matchResult ->
            val chords = matchResult.groupValues[1].split(" ", "\n")
            chords.forEach { chord ->
                if (chord.isNotEmpty() && !detector.isWordAChord(chord)) {
                    val placeholder = uiResourceService.resString(R.string.chords_unknown_chord)
                    val errorMessage = placeholder.format(chord)
                    throw ChordsValidationError(errorMessage)
                }
            }
            ""
        }
    }

    private fun chordsFisTofSharp() {
        transformChords { chord ->
            chord.replace(Regex("""(\w)is"""), "$1#")
        }
    }

    private fun moveChordsAboveToRight() {
        reformatAndTrim()
        transformLyrics { lyrics ->
            var c = "\n" + lyrics + "\n"
            c = c.replace(Regex("""\n\[(.+)]\n(.+)\n"""), "\n$2 [$1]\n")
            c.drop(1).dropLast(1)
        }
    }

    private fun reformatAndTrim() {
        transformLines { line ->
            line.trim()
                    .replace("\r\n", "\n")
                    .replace("\r", "\n")
                    .replace("\t", " ")
                    .replace("\u00A0", " ")
                    .replace(Regex("""\[+"""), "[")
                    .replace(Regex("""]+"""), "]")
                    .replace(Regex("""\[ +"""), "[")
                    .replace(Regex(""" +]"""), "]")
                    .replace(Regex("""] ?\["""), " ") // join adjacent chords
                    .replace(Regex("""\[]"""), "")
                    .replace(Regex(""" +"""), " ") // double+ spaces
        }
        transformLyrics { lyrics ->
            lyrics.replace(Regex("\n\n+"), "\n\n") // max 1 empty line
                    .replace(Regex("^\n+"), "")
                    .replace(Regex("\n+$"), "")
        }
    }

    private fun transformLyrics(transformer: (String) -> String) {
        val text = contentEdit!!.text.toString()
        contentEdit!!.setText(transformer.invoke(text))
    }

    private fun transformLines(transformer: (String) -> String) {
        val text = contentEdit!!.text.toString()
                .lines().joinToString(separator = "\n") { line ->
                    transformer.invoke(line)
                }
        contentEdit!!.setText(text)
    }

    private fun transformChords(transformer: (String) -> String) {
        val text = contentEdit!!.text.toString()
                .replace(Regex("""\[(.*?)]""")) { matchResult ->
                    "[" + transformer.invoke(matchResult.groupValues[1]) + "]"
                }
        contentEdit!!.setText(text)
    }

    private fun detectChords() {
        reformatAndTrim()
        val detector = ChordsDetector(chordsNotation)
        transformLyrics { lyrics ->
            detector.findChords(lyrics)
        }
    }

    private fun chooseChordsNotation() {
        val actions = ChordsNotation.values().map { notation ->
            ContextMenuBuilder.Action(notation.displayNameResId) { selectChordsNotation(notation) }
        }
        contextMenuBuilder.showContextMenu(R.string.settings_chords_notation, actions)
    }

    private fun selectChordsNotation(notation: ChordsNotation) {
        chordsNotation = notation
    }

    private fun onCopyChordClick() {
        val edited = contentEdit!!.text.toString()
        val selStart = contentEdit!!.selectionStart
        val selEnd = contentEdit!!.selectionEnd

        var selection = edited.substring(selStart, selEnd).trim()
        if (selection.startsWith("["))
            selection = selection.drop(1)
        if (selection.endsWith("]"))
            selection = selection.dropLast(1)
        clipboardChords = selection.trim()

        if (clipboardChords.isNullOrEmpty()) {
            uiInfoService.showToast(R.string.no_chords_selected)
        } else {
            uiInfoService.showToast(uiResourceService.resString(R.string.chords_copied, clipboardChords))
        }
    }

    private fun onPasteChordClick() {
        history.save(contentEdit!!)
        if (clipboardChords.isNullOrEmpty()) {
            uiInfoService.showToast(R.string.paste_chord_empty)
            return
        }

        var edited = contentEdit!!.text.toString()
        val selStart = contentEdit!!.selectionStart
        var selEnd = contentEdit!!.selectionEnd
        val before = edited.take(selStart)
        val after = edited.drop(selEnd)

        edited = "$before[$clipboardChords]$after"
        selEnd = selStart + 2 + clipboardChords!!.length

        setContentWithSelection(edited, selStart, selEnd)
    }

    private fun addChordSplitter() {
        history.save(contentEdit!!)
        val edited = contentEdit!!.text.toString()
        val selStart = contentEdit!!.selectionStart
        val before = edited.take(selStart)
        // count previous opening and closing brackets
        val opening = before.count { c -> c == '[' }
        val closing = before.count { c -> c == ']' }

        if (opening > closing) {
            onAddSequenceClick("]")
        } else {
            onAddSequenceClick("[")
        }
    }

    private fun onAddSequenceClick(s: String) {
        var edited = contentEdit!!.text.toString()
        var selStart = contentEdit!!.selectionStart
        var selEnd = contentEdit!!.selectionEnd
        val before = edited.take(selStart)
        val after = edited.drop(selEnd)

        edited = "$before$s$after"
        selStart += s.length
        selEnd = selStart

        setContentWithSelection(edited, selStart, selEnd)
    }

    private fun onAddChordClick() {
        history.save(contentEdit!!)

        var edited = contentEdit!!.text.toString()
        var selStart = contentEdit!!.selectionStart
        var selEnd = contentEdit!!.selectionEnd
        val before = edited.take(selStart)
        val after = edited.drop(selEnd)

        // if there's nonempty selection
        if (selStart < selEnd) {
            val selected = edited.substring(selStart, selEnd)
            edited = "$before[$selected]$after"
            selStart++
            selEnd++
        } else { // just single cursor
            // if it's the end of line AND there is no space before
            if ((after.isEmpty() || after.startsWith("\n")) && !before.isEmpty() && !before.endsWith(" ") && !before.endsWith("\n")) {
                // insert missing space
                edited = "$before []$after"
                selStart += 2
            } else {
                edited = "$before[]$after"
                selStart += 1
            }
            selEnd = selStart
        }

        setContentWithSelection(edited, selStart, selEnd)
    }

    private fun setContentWithSelection(edited: String, selStart: Int, selEnd: Int) {
        contentEdit?.setText(edited)
        contentEdit?.setSelection(selStart, selEnd)
        contentEdit?.requestFocus()
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

    override fun getLayoutState(): LayoutState {
        return LayoutState.CUSTOM_SONG_EDIT
    }

    override fun getLayoutResourceId(): Int {
        return R.layout.chords_editor
    }

    override fun onBackClicked() {
        returnNewContent()
    }

    fun setContent(content: String, chordsNotation: ChordsNotation?) {
        this.chordsNotation = chordsNotation
        var content2 = content
        if (chordsNotation != null) {
            val converter = ChordsConverter(ChordsNotation.default, chordsNotation)
            content2 = converter.convertLyrics(content)
        }
        val length = content2.length
        setContentWithSelection(content2, length, length)
        history.reset(contentEdit!!)
        softKeyboardService.showSoftKeyboard(contentEdit)
    }

    private fun returnNewContent() {
        var content = contentEdit?.text.toString()
        if (chordsNotation != null) {
            val converter = ChordsConverter(chordsNotation!!, ChordsNotation.default)
            content = converter.convertLyrics(content)
        }
        layoutController.showCustomSong()
        customSongEditLayoutController.get().setSongContent(content)
    }

    override fun onLayoutExit() {
        softKeyboardService.hideSoftKeyboard()
    }
}
