package igrek.songbook.info.errorcheck


class SafeExecutor(
        action: () -> Unit,
) {

    init {
        execute(action)
    }

    private fun execute(action: () -> Unit) {
        try {
            action.invoke()
        } catch (t: Throwable) {
            UiErrorHandler().handleError(t)
        }
    }

}
