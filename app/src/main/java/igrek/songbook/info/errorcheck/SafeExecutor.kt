package igrek.songbook.info.errorcheck

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


inline fun safeExecute(block: () -> Unit) {
    try {
        block()
    } catch (t: Throwable) {
        UiErrorHandler().handleError(t)
    }
}

fun safeAsyncExecutor(block: suspend () -> Unit): () -> Unit = {
    GlobalScope.launch {
        try {
            block()
        } catch (t: Throwable) {
            UiErrorHandler().handleError(t)
        }
    }
}
