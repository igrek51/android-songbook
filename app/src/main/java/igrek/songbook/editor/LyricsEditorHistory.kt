package igrek.songbook.editor

open class LyricsEditorHistory {

    data class Entry(
        val text: String,
        val startSelection: Int,
        val endSelection: Int,
    )

    private var history: MutableList<Entry> = mutableListOf()

    fun reset(textEditor: ITextEditor) {
        history = mutableListOf()
        save(textEditor)
    }

    fun isEmpty(): Boolean = history.isEmpty()

    fun save(textEditor: ITextEditor) {
        val text = textEditor.getText()
        val (selStart, selEnd) = textEditor.getSelection()
        val current = Entry(text, selStart, selEnd)
        if (history.isNotEmpty() && history.last().text == text) {
            // update cursor position
            history[history.lastIndex] = current
        } else {
            // brand new history entry when text is different or it's first
            history.add(current)
        }
    }

    fun revertLast(textEditor: ITextEditor) {
        val last = history.last()
        val text = last.text
        var selStart = last.startSelection
        var selEnd = last.endSelection
        textEditor.setText(last.text)
        if (selStart > text.length)
            selStart = text.length
        if (selEnd > text.length)
            selEnd = text.length
        textEditor.setSelection(selStart, selEnd)
        history.removeAt(history.lastIndex)
    }

    private fun peekLastSelection(): Pair<Int, Int>? {
        if (history.isEmpty())
            return null
        val last = history.last()
        return Pair(last.startSelection, last.endSelection)
    }


    fun restoreSelectionFromHistory(textEditor: ITextEditor) {
        val lastSelection = peekLastSelection()
        if (lastSelection != null) {
            var selStart = lastSelection.first
            var selEnd = lastSelection.second
            val maxLength = textEditor.getText().length
            if (selStart > maxLength)
                selStart = maxLength
            if (selEnd > maxLength)
                selEnd = maxLength
            textEditor.setSelection(selStart, selEnd)
        }
    }
}