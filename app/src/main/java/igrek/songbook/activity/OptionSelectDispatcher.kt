package igrek.songbook.activity

import android.util.SparseArray
import igrek.songbook.info.errorcheck.SafeExecutor

class OptionSelectDispatcher {

    private val optionActions = SparseArray<() -> Unit>()

    fun optionsSelect(id: Int): Boolean {
        if (optionActions.get(id) != null) {
            val action = optionActions.get(id)
            SafeExecutor(action = action)
            return true
        }
        return false
    }
}
