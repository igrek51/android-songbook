package igrek.songbook.info.errorcheck

import android.os.Handler
import android.os.Looper
import igrek.songbook.info.logger.LoggerFactory

class RetryDelayed(
    private val retries: Int,
    private val delayMs: Long,
    private val errorType: Class<out Throwable>,
    private val action: () -> Unit
) {
    private var attempt = 0

    init {
        makeAttempt()
    }

    private fun makeAttempt() {
        try {
            action()
        } catch (t: Throwable) {
            if (!errorType.isInstance(t)) {
                throw t
            }

            if (attempt++ < retries) {
                LoggerFactory.logger.debug("Attempt $attempt/$retries failed, retrying in $delayMs ms")
                Handler(Looper.getMainLooper()).postDelayed({
                    makeAttempt()
                }, delayMs)
            } else {
                throw RuntimeException("", t)
            }
        }
    }

}
