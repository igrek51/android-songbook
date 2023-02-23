package igrek.songbook.info.errorcheck

import kotlinx.coroutines.runBlocking


class SafeExecutor(
    action: suspend () -> Unit,
) {

    init {
        execute(action)
    }

    private fun execute(action: suspend () -> Unit) {
        try {
            runBlocking {
                action.invoke()
            }
        } catch (t: Throwable) {
            UiErrorHandler().handleError(t)
        }
    }

}

inline fun safeExecute(block: () -> Unit) {
    try {
        block()
    } catch (t: Throwable) {
        UiErrorHandler().handleError(t)
    }
}
