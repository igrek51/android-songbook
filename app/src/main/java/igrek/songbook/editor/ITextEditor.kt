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

    override fun setText(text: String) {}

    override fun getText(): String = ""

    override fun setSelection(start: Int, end: Int) {}

    override fun getSelection(): Pair<Int, Int> = 0 to 0

}