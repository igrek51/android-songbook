package igrek.songbook.editor

import igrek.songbook.R
import igrek.songbook.chords.converter.ChordsConverter
import igrek.songbook.chords.detector.ChordsDetector
import igrek.songbook.chords.syntax.ChordNameProvider
import igrek.songbook.info.UiInfoService
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.layout.contextmenu.ContextMenuBuilder
import igrek.songbook.settings.chordsnotation.ChordsNotation
import igrek.songbook.system.ClipboardManager

class ChordsEditorTransformer(
        private var history: LyricsEditorHistory,
        private val chordsNotation: ChordsNotation?,
        private val textEditor: ITextEditor,
        uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
        clipboardManager: LazyInject<ClipboardManager> = appFactory.clipboardManager,
) {
    private val uiInfoService by LazyExtractor(uiInfoService)
    private val clipboardManager by LazyExtractor(clipboardManager)

    private var clipboard: String? = null

    private fun transformLyrics(transformer: (String) -> String) {
        textEditor.getText()
                .let { transformer.invoke(it) }
                .let { textEditor.setText(it) }
    }

    private fun transformLines(transformer: (String) -> String) {
        textEditor.getText()
                .lines()
                .joinToString(separator = "\n") { line ->
                    transformer.invoke(line)
                }
                .let { textEditor.setText(it) }
    }

    private fun transformDoubleLines(input: String, transformer: (String, String) -> List<String>?): String {
        val inputLines = input.lines()
        val lines = mutableListOf<String>()
        var i = 0
        while (i < inputLines.size) {
            val line = inputLines[i++]
            val next = inputLines.getOrNull(i)
            if (next == null) {
                lines.add(line)
                continue
            }
            val result = transformer.invoke(line, next)
            if (result == null) {
                lines.add(line)
            } else {
                lines.addAll(result)
                i++
            }
        }
        return lines.joinToString(separator = "\n")
    }

    private fun transformChords(transformer: (String) -> String) {
        textEditor.getText()
                .replace(Regex("""\[(.*?)]""")) { matchResult ->
                    "[" + transformer.invoke(matchResult.groupValues[1]) + "]"
                }
                .let { textEditor.setText(it) }
    }

    private fun setContentWithSelection(edited: String, selStart: Int, selEnd: Int) {
        textEditor.setText(edited)
        textEditor.setSelection(selStart, selEnd)
    }

    private fun onAddSequenceClick(s: String) {
        var edited = textEditor.getText()
        var (selStart, selEnd) = textEditor.getSelection()
        val before = edited.take(selStart)
        val after = edited.drop(selEnd)

        edited = "$before$s$after"
        selStart += s.length
        selEnd = selStart

        textEditor.setText(edited)
        textEditor.setSelection(selStart, selEnd)
    }

    fun onWrapChordClick() {
        history.save(textEditor)

        var edited = textEditor.getText()
        var (selStart, selEnd) = textEditor.getSelection()
        val before = edited.take(selStart)
        val after = edited.drop(selEnd)

        // if there's nonempty selection
        if (selStart < selEnd) {
            val selected = edited.substring(selStart, selEnd)
            edited = "$before[$selected]$after"
            selStart++
            selEnd++
        } else { // just single cursor
            // clicked twice accidentaly
            if (before.endsWith("[") && after.startsWith("]")) {
                return
            }
            // if it's the end of line AND there is no space before
            if ((after.isEmpty() || after.startsWith("\n")) && before.isNotEmpty() && !before.endsWith(" ") && !before.endsWith("\n")) {
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
        val chordNameProvider = ChordNameProvider()
        val falseFriends: Set<String> = when {
            chordsNotation != null -> chordNameProvider.falseFriends(chordsNotation)
            else -> emptySet()
        }
        text.replace(Regex("""\[((.|\n)+?)]""")) { matchResult ->
            validateChordsGroup(matchResult.groupValues[1], detector, falseFriends)
            ""
        }
    }

    private fun validateChordsGroup(chordsGroup: String, detector: ChordsDetector, falseFriends: Set<String>) {
        val chords = chordsGroup.split(" ", "\n", "(", ")")
        chords.forEach { chord ->
            if (chord.isNotEmpty()) {
                if (!detector.isWordAChord(chord) || chord in falseFriends) {
                    val errorMessage = uiInfoService.resString(R.string.chords_unknown_chord, chord)
                    throw ChordsValidationError(errorMessage)
                }
            }
        }
    }

    fun convertFromOtherNotationDialog() {
        val actions = ChordsNotation.values().map { notation ->
            ContextMenuBuilder.Action(notation.displayNameResId) {
                convertFromNotation(notation)
            }
        }
        ContextMenuBuilder().showContextMenu(R.string.chords_editor_convert_from_notation, actions)
    }

    private fun convertFromNotation(fromNotation: ChordsNotation) {
        val converter = ChordsConverter(fromNotation, chordsNotation ?: ChordsNotation.default)
        val converted = converter.convertLyrics(textEditor.getText())
        textEditor.setText(converted)
    }


    fun validateChords() {
        val errorMessage = quietValidate()
        if (errorMessage == null) {
            uiInfoService.showToast(R.string.chords_are_valid)
        } else {
            val placeholder = uiInfoService.resString(R.string.editor_chords_invalid)
            uiInfoService.showToast(placeholder.format(errorMessage))
        }
    }

    fun quietValidate(): String? {
        val text = textEditor.getText()
        return try {
            validateChordsBrackets(text)
            validateChordsNotation(text)

            if (reformatNeeded()) {
                val errorMessage = uiInfoService.resString(R.string.editor_reformat_error)
                throw ChordsValidationError(errorMessage)
            }

            null
        } catch (e: ChordsValidationError) {
            var errorMessage = e.errorMessage
            if (errorMessage.isNullOrEmpty())
                errorMessage = uiInfoService.resString(e.messageResId!!)
            errorMessage
        }
    }


    fun onCopyClick() {
        val edited = textEditor.getText()
        val (selStart, selEnd) = textEditor.getSelection()

        val selection = edited.substring(selStart, selEnd)
        clipboard = selection
        clipboardManager.copyToSystemClipboard(selection)

        if (clipboard.isNullOrEmpty()) {
            uiInfoService.showToast(R.string.no_text_selected)
        } else {
            uiInfoService.showToast(uiInfoService.resString(R.string.selected_text_copied, clipboard))
        }
    }

    fun onPasteClick() {
        history.save(textEditor)

        val systemClipboard = clipboardManager.getFromSystemClipboard()
        val toPaste = if (!systemClipboard.isNullOrEmpty()) systemClipboard else clipboard
        if (toPaste.isNullOrEmpty()) {
            uiInfoService.showToast(R.string.paste_empty)
            return
        }

        var edited = textEditor.getText()
        var (selStart, selEnd) = textEditor.getSelection()
        val before = edited.take(selStart)
        val after = edited.drop(selEnd)

        edited = "$before$toPaste$after"
        selEnd = selStart + toPaste.length
        selStart = selEnd

        setContentWithSelection(edited, selStart, selEnd)
    }

    fun addChordSplitter() {
        history.save(textEditor)
        val edited = textEditor.getText()
        val (selStart, _) = textEditor.getSelection()
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


    fun detectChords(keepIndentation: Boolean = false) {
        val chordsMarker = ChordsMarker(ChordsDetector(chordsNotation))
        transformLyrics { lyrics ->
            chordsMarker.detectAndMarkChords(lyrics, keepIndentation)
        }
        val detectedChordsNum = chordsMarker.allMarkedChords.size
        if (detectedChordsNum == 0) {
            // find chords from other notations as well
            val text = textEditor.getText()
            val genericChordsMarker = ChordsMarker(ChordsDetector())
            genericChordsMarker.detectAndMarkChords(text, keepIndentation)
            val otherChordsDetected = genericChordsMarker.allMarkedChords
            if (otherChordsDetected.isNotEmpty()) {
                val message = uiInfoService.resString(R.string.editor_other_chords_detected, otherChordsDetected.joinToString())
                uiInfoService.showToast(message)
            } else {
                uiInfoService.showToast(R.string.no_new_chords_detected)
            }
        } else {
            uiInfoService.showToast(uiInfoService.resString(R.string.new_chords_detected, detectedChordsNum.toString()))
        }
    }


    fun chordsFisTofSharp() {
        transformChords { chord ->
            chord.replace(Regex("""(\w)is"""), "$1#")
        }
    }

    fun moveChordsAboveToRight() {
        transformLyrics(this::transformMoveChordsAboveToRight)
    }

    fun moveChordsAboveToInline() {
        transformLyrics(this::transformMoveChordsAboveToInline)
    }

    fun transformMoveChordsAboveToRight(lyrics: String): String {
        return transformDoubleLines(lyrics) { first: String, second: String ->
            if (first.hasOnlyChords() && second.isNotBlank() && !second.hasChords()) {
                val trimmedChords = first.trimChordsLine()
                val joined = "$second $trimmedChords"
                return@transformDoubleLines listOf(joined)
            }
            null
        }
    }

    fun transformMoveChordsAboveToInline(lyrics: String): String {
        return transformDoubleLines(lyrics) { first: String, second: String ->
            if (first.hasOnlyChords() && second.isNotBlank() && !second.hasChords()) {
                val chords = ChordSegmentDetector().detectChords(first)
                val joined = ChordSegmentApplier().applyChords(second, chords)
                return@transformDoubleLines listOf(joined)
            }
            null
        }
    }

    private fun transformDetectAndMoveChordsAboveToInline(lyrics: String): String {
        val chordsMarker = ChordsMarker(ChordsDetector(chordsNotation))
        return transformDoubleLines(lyrics) { first: String, second: String ->
            if (first.isNotBlank() && second.isNotBlank() && !first.hasChords() && !second.hasChords()) {
                val firstRecogized = chordsMarker.detectAndMarkChords(first, keepIndentation = true)
                if (firstRecogized.hasOnlyChords()) {
                    val chords = ChordSegmentDetector().detectUnmarkedChords(first)
                    val joined = ChordSegmentApplier().applyChords(second, chords)
                    return@transformDoubleLines listOf(joined)
                }
            }
            null
        }
    }

    private fun String.hasChords(): Boolean {
        return "[" in this && "]" in this
    }

    private fun String.hasOnlyChords(): Boolean {
        return "[" in this && "]" in this && this.replace(Regex("""\[(.*?)]"""), "").trim().isBlank()
    }

    private fun String.trimChordsLine(): String {
        return this.trim()
                .replace(Regex("""\[ +"""), "[")
                .replace(Regex(""" +]"""), "]")
                .replace(Regex(""" +"""), " ")
    }

    fun reformatAndTrimEditor() {
        transformLyrics(this::reformatAndTrim)
    }

    private fun reformatNeeded(): Boolean {
        val original = textEditor.getText()
        return original != reformatAndTrim(original)
    }

    fun reformatAndTrim(lyrics: String): String {
        return lyrics.lines()
                .joinToString(separator = "\n") { line ->
                    line.trim()
                            .replace("\r\n", "\n")
                            .replace("\r", "\n")
                            .replace("\t", " ")
                            .replace("\u00A0", " ")
                            .replace("\uFFFD", "")
                            .replace(Regex("""\[+"""), "[")
                            .replace(Regex("""]+"""), "]")
                            .replace(Regex("""\[ +"""), "[")
                            .replace(Regex(""" +]"""), "]")
                            .replace(Regex("""\[]"""), "")
                            .replace(Regex(""" +"""), " ") // double+ spaces
                            .replace(Regex("""] ?\["""), " ") // join adjacent chords
                }
                .replace("\r\n", "\n")
                .replace("\r", "\n")
                .replace(Regex("\n\n+"), "\n\n") // max 1 empty line
                .replace(Regex("^\n+"), "")
                .replace(Regex("\n+$"), "")
    }

    fun removeDoubleEmptyLines() {
        transformLyrics(this::transformRemoveDoubleEmptyLines)
    }

    fun transformRemoveDoubleEmptyLines(lyrics: String): String {
        return lyrics.replace("\r\n", "\n")
                .replace("\r", "\n")
                .replace(Regex("""\n[ \t\f]*\n"""), "\n")
    }

    fun duplicateSelection() {
        val text = textEditor.getText()
        // expand to closest lines
        val (start, end) = when {
            hasAnySelection() -> textEditor.getSelection()
            else -> {
                val (selStart, selEnd) = textEditor.getSelection()
                val (start1, _) = findLineRange(text, selStart)
                val (_, end2) = findLineRange(text, selEnd)
                start1 to end2
            }
        }

        var copied = text.substring(start, end)
        val prefix = text.take(end)
        val suffix = text.drop(end)

        if (end == text.length && !copied.startsWith("\n") && !copied.endsWith("\n"))
            copied = "\n$copied"

        val result = prefix + copied + suffix

        textEditor.setText(result)
        textEditor.setSelection(end, end + copied.length)
    }

    fun selectNextLine() {
        val text = textEditor.getText()
        val (selStart, selEnd) = textEditor.getSelection()
        if (hasAnySelection()) {
            val (start1, _) = findLineRange(text, selStart)
            val (_, end2) = findLineRange(text, selEnd)
            textEditor.setSelection(start1, end2)
        } else {
            val (start, end) = findLineRange(text, selStart)
            textEditor.setSelection(start, end)
        }
    }

    private fun hasAnySelection(): Boolean {
        val (selStart, selEnd) = textEditor.getSelection()
        return selStart != selEnd
    }

    private fun findLineRange(text: String, at: Int): Pair<Int, Int> {
        val before = text.take(at)
        val after = text.drop(at)
        val beforeIndex = before.lastIndexOf('\n') + 1 // even for -1
        val afterIndex2 = when (val afterIndex = after.indexOf('\n')) {
            -1 -> text.length
            else -> afterIndex + before.length + 1
        }
        return beforeIndex to afterIndex2.trimTo(text.length)
    }

    fun unmarkChords() {
        transformLyrics { lyrics ->
            lyrics.replace("[", "").replace("]", "")
        }
    }

    private fun Int.trimTo(max: Int): Int {
        return if (this > max) max else this
    }

    fun removeBracketsContent() {
        transformLyrics { lyrics ->
            lyrics.replace(Regex("""\[(.|\n)+?]"""), "")
        }
    }

    fun detectAndMoveChordsAboveToInline() {
        transformLyrics(this::transformDetectAndMoveChordsAboveToInline)
    }

}

