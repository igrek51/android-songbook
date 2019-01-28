package igrek.songbook.activity

import android.util.SparseArray

import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.errorcheck.SafeExecutor

class OptionSelectDispatcher {

    private val optionActions = SparseArray<() -> Unit>()

    init {
        DaggerIoc.getFactoryComponent().inject(this)
    }

    fun optionsSelect(id: Int): Boolean {
        if (optionActions.get(id) != null) {
            val action = optionActions.get(id)
            SafeExecutor().execute(action)
            return true
        }
        return false
    }
}
