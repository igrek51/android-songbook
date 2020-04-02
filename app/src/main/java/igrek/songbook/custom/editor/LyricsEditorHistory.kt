package igrek.songbook.custom.editor

import android.widget.EditText

open class LyricsEditorHistory {

    data class Entry(val text: String,
                     val startSelection: Int,
                     val endSelection: Int)

    private var history: MutableList<Entry> = mutableListOf()

    fun reset(edittext: EditText) {
        history = mutableListOf()
        save(edittext)
    }

    fun isEmpty(): Boolean = history.isEmpty()

    fun save(edittext: EditText) {
        val text = edittext.text.toString()
        // only if it's different than previous one
        if (history.lastOrNull()?.text != text) {
            val entry = Entry(text, edittext.selectionStart, edittext.selectionEnd)
            history.add(entry)
        }
    }

    fun revertLast(edittext: EditText) {
        val last = history.last()
        val text = last.text
        var selStart = last.startSelection
        var selEnd = last.endSelection
        edittext.setText(last.text)
        if (selStart > text.length)
            selStart = text.length
        if (selEnd > text.length)
            selEnd = text.length
        edittext.setSelection(selStart, selEnd)
        edittext.requestFocus()
        history.removeAt(history.lastIndex)
    }

    fun peekLastSelection(): Pair<Int, Int>? {
        if (history.isNullOrEmpty())
            return null
        val last = history.last()
        return Pair(last.startSelection, last.endSelection)
    }
}