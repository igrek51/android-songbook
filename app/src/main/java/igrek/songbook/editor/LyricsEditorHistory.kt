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

    fun revertLast(textEditor: ITextEditor): Boolean {
        if (history.isEmpty())
            return false

        val last = history.last()

        // revert to last saved state without dropping it
        if (textEditor.getText() != last.text) {
            textEditor.setText(last.text)
            textEditor.setSelection(last.startSelection, last.endSelection)
            return true
        }

        if (history.size <= 1)
            return false

        history.dropLast(1)
        val veryLast = history.last()
        textEditor.setText(veryLast.text)
        textEditor.setSelection(veryLast.startSelection, veryLast.endSelection)
        return true
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