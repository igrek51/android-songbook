package igrek.songbook.editor

import androidx.compose.runtime.MutableState
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import igrek.songbook.util.clamp

interface ITextEditor {
    fun setText(text: String)
    fun getText(): String
    fun setSelection(start: Int, end: Int)
    fun getSelection(): Pair<Int, Int>
}

class EditTextTextEditor(private val component: MutableState<TextFieldValue>) : ITextEditor {

    override fun setText(text: String) {
        val formerSelection = component.value.selection
        val newSelection = when {
            formerSelection.min <= text.length && formerSelection.max <= text.length -> formerSelection
            else -> TextRange(start = text.length, end = text.length) // cursor at the end
        }
        component.value = TextFieldValue(
            text = text,
            selection = newSelection
        )
    }

    override fun getText(): String {
        return component.value.text
    }

    override fun setSelection(start: Int, end: Int) {
        val maxIndex = component.value.text.length
        val start2 = start.clamp(0, maxIndex)
        val end2 = end.clamp(0, maxIndex)
        component.value = component.value.copy(selection = TextRange(start2, end2))
    }

    override fun getSelection(): Pair<Int, Int> {
        val min = component.value.selection.min
        val max = component.value.selection.max
        return min to max
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