package igrek.songbook.editor

import android.widget.EditText

interface ITextEditor {
    fun setText(text: String)
    fun getText(): String
    fun setSelection(start: Int, end: Int)
    fun getSelection(): Pair<Int, Int>
}

class EditTextTextEditor(private val component: EditText) : ITextEditor {

    override fun setText(text: String) {
        component.setText(text)
    }

    override fun getText(): String {
        return component.text.toString()
    }

    override fun setSelection(start: Int, end: Int) {
        component.setSelection(start, end)
        component.requestFocus()
    }

    override fun getSelection(): Pair<Int, Int> {
        return component.selectionStart to component.selectionEnd
    }

}

class EmptyTextEditor : ITextEditor {
    private var text = ""
    private var selStart = 0
    private var selEnd = 0

    override fun setText(text: String) {
        this.text = text
    }

    override fun getText(): String = this.text

    override fun setSelection(start: Int, end: Int) {
        this.selStart = start
        this.selEnd = end
    }

    override fun getSelection(): Pair<Int, Int> = this.selStart to this.selEnd
}