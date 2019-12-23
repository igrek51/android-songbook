package igrek.songbook.util

import igrek.songbook.info.logger.LoggerFactory

class RetryDelayed(
        retries: Int,
        delayMs: Long,
        errorType: Class<out Throwable>,
        action: () -> Unit
) {

    init {
        var attempt = 0
        while (true) {
            try {
                action()
                break
            } catch (t: Throwable) {
                if (!errorType.isInstance(t)) {
                    throw t
                }

                if (attempt++ < retries) {
                    LoggerFactory.logger.debug("Attempt $attempt/$retries failed, retrying in $delayMs ms")
                    Thread.sleep(delayMs)
                } else {
                    throw RuntimeException("", t)
                }
            }
        }
    }

}
