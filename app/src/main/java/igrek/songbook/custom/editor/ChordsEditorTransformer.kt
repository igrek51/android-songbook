package igrek.songbook.custom.editor

import android.widget.EditText
import igrek.songbook.R
import igrek.songbook.chords.converter.ChordsConverter
import igrek.songbook.chords.detector.ChordsDetector
import igrek.songbook.chords.syntax.ChordNameProvider
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.UiResourceService
import igrek.songbook.layout.contextmenu.ContextMenuBuilder
import igrek.songbook.settings.chordsnotation.ChordsNotation

class ChordsEditorTransformer(
        private var contentEdit: EditText,
        private var history: LyricsEditorHistory,
        private val chordsNotation: ChordsNotation?,
        private val uiResourceService: UiResourceService,
        private val uiInfoService: UiInfoService,
) {
    private var clipboardChords: String? = null

    private fun transformLyrics(transformer: (String) -> String) {
        val text = contentEdit.text.toString()
        contentEdit.setText(transformer.invoke(text))
    }

    private fun transformLines(transformer: (String) -> String) {
        val text = contentEdit.text.toString()
                .lines().joinToString(separator = "\n") { line ->
                    transformer.invoke(line)
                }
        contentEdit.setText(text)
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
        val text = contentEdit.text.toString()
                .replace(Regex("""\[(.*?)]""")) { matchResult ->
                    "[" + transformer.invoke(matchResult.groupValues[1]) + "]"
                }
        contentEdit.setText(text)
    }

    fun setContentWithSelection(edited: String, selStart: Int, selEnd: Int) {
        contentEdit.setText(edited)
        contentEdit.setSelection(selStart, selEnd)
        contentEdit.requestFocus()
    }

    private fun onAddSequenceClick(s: String) {
        var edited = contentEdit.text.toString()
        var selStart = contentEdit.selectionStart
        var selEnd = contentEdit.selectionEnd
        val before = edited.take(selStart)
        val after = edited.drop(selEnd)

        edited = "$before$s$after"
        selStart += s.length
        selEnd = selStart

        setContentWithSelection(edited, selStart, selEnd)
    }

    fun onWrapChordClick() {
        history.save(contentEdit)

        var edited = contentEdit.text.toString()
        var selStart = contentEdit.selectionStart
        var selEnd = contentEdit.selectionEnd
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
                    val placeholder = uiResourceService.resString(R.string.chords_unknown_chord)
                    val errorMessage = placeholder.format(chord)
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
        val converted = converter.convertLyrics(contentEdit.text.toString())
        contentEdit.setText(converted)
    }


    fun validateChords() {
        val errorMessage = quietValidate()
        if (errorMessage == null) {
            uiInfoService.showToast(R.string.chords_are_valid)
        } else {
            val placeholder = uiResourceService.resString(R.string.chords_invalid)
            uiInfoService.showToast(placeholder.format(errorMessage))
        }
    }

    fun quietValidate(): String? {
        val text = contentEdit.text.toString()
        return try {
            validateChordsBrackets(text)
            validateChordsNotation(text)
            null
        } catch (e: ChordsValidationError) {
            var errorMessage = e.errorMessage
            if (errorMessage.isNullOrEmpty())
                errorMessage = uiResourceService.resString(e.messageResId!!)
            errorMessage
        }
    }


    fun onCopyChordClick() {
        val edited = contentEdit.text.toString()
        val selStart = contentEdit.selectionStart
        val selEnd = contentEdit.selectionEnd

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

    fun onPasteChordClick() {
        history.save(contentEdit)
        if (clipboardChords.isNullOrEmpty()) {
            uiInfoService.showToast(R.string.paste_chord_empty)
            return
        }

        var edited = contentEdit.text.toString()
        val selStart = contentEdit.selectionStart
        var selEnd = contentEdit.selectionEnd
        val before = edited.take(selStart)
        val after = edited.drop(selEnd)

        edited = "$before[$clipboardChords]$after"
        selEnd = selStart + 2 + clipboardChords!!.length

        setContentWithSelection(edited, selStart, selEnd)
    }

    fun addChordSplitter() {
        history.save(contentEdit)
        val edited = contentEdit.text.toString()
        val selStart = contentEdit.selectionStart
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


    fun detectChords() {
        val chordsMarker = ChordsMarker(ChordsDetector(chordsNotation))
        transformLyrics { lyrics ->
            chordsMarker.detectAndMarkChords(lyrics)
        }
        val detectedChordsNum = chordsMarker.allMarkedChords.size
        if (detectedChordsNum == 0) {
            // find chords from other notations as well
            val text = contentEdit.text.toString()
            val genericChordsMarker = ChordsMarker(ChordsDetector())
            genericChordsMarker.detectAndMarkChords(text)
            val otherChordsDetected = genericChordsMarker.allMarkedChords
            if (otherChordsDetected.isNotEmpty()) {
                val message = uiResourceService.resString(R.string.editor_other_chords_detected, otherChordsDetected.joinToString())
                uiInfoService.showToast(message)
            } else {
                uiInfoService.showToast(R.string.no_new_chords_detected)
            }
        } else {
            uiInfoService.showToast(uiResourceService.resString(R.string.new_chords_detected, detectedChordsNum.toString()))
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

    fun reformatAndTrim() {
        transformLines { line ->
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
        transformLyrics { lyrics ->
            lyrics.replace("\r\n", "\n")
                    .replace("\r", "\n")
                    .replace(Regex("\n\n+"), "\n\n") // max 1 empty line
                    .replace(Regex("^\n+"), "")
                    .replace(Regex("\n+$"), "")
        }
    }

    fun removeDoubleEmptyLines() {
        transformLyrics { lyrics ->
            lyrics.replace("\r\n", "\n")
                    .replace("\r", "\n")
                    .replace(Regex("""\n\s*\n"""), "\n")
        }
    }

    fun duplicateSelection() {
        val text = contentEdit.text.toString()
        // expand to closest lines
        val (start1, _) = findLineRange(text, contentEdit.selectionStart)
        val (_, end2) = findLineRange(text, contentEdit.selectionEnd)

        val selected = text.substring(start1, end2)
        val result = text.take(end2) + "\n" + selected + text.drop(end2)

        val newStart = end2 + 1
        val newEnd = end2 + 1 + selected.length

        contentEdit.setText(result)
        contentEdit.setSelection(newStart, newEnd)
        contentEdit.requestFocus()
    }

    fun selectNextLine() {
        val text = contentEdit.text.toString()
        if (hasAnySelection()) {
            val (start1, _) = findLineRange(text, contentEdit.selectionStart)
            val nextEndOffset = if (start1 == contentEdit.selectionStart) 1 else 0
            val (_, end2) = findLineRange(text, contentEdit.selectionEnd + nextEndOffset)
            contentEdit.setSelection(start1, end2)
            contentEdit.requestFocus()
        } else {
            val (start, end) = findLineRange(text, contentEdit.selectionStart)
            contentEdit.setSelection(start, end)
            contentEdit.requestFocus()
        }
    }

    private fun hasAnySelection(): Boolean {
        return contentEdit.run { selectionStart != selectionEnd }
    }

    private fun findLineRange(text: String, at: Int): Pair<Int, Int> {
        val before = text.take(at)
        val after = text.drop(at)
        val beforeIndex = before.lastIndexOf('\n') + 1 // even for -1
        var afterIndex = after.indexOf('\n')
        if (afterIndex == -1)
            afterIndex = text.length
        else
            afterIndex += before.length
        return beforeIndex to afterIndex
    }

}