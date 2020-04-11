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
        // only if it's different than previous one
        if (history.lastOrNull()?.text != text) {
            val (selStart, selEnd) = textEditor.getSelection()
            val entry = Entry(text, selStart, selEnd)
            history.add(entry)
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

    fun peekLastSelection(): Pair<Int, Int>? {
        if (history.isNullOrEmpty())
            return null
        val last = history.last()
        return Pair(last.startSelection, last.endSelection)
    }
}