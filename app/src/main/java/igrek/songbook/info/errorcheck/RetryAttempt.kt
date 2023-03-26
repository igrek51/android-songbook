package igrek.songbook.info.errorcheck

import igrek.songbook.info.logger.LoggerFactory
import kotlinx.coroutines.delay

class RetryAttempt(
    private val retries: Int,
    private val contextMessage: String,
    private val backoffDelayMs: Long = 0,
) {
    private var attempt = 0

    suspend fun run(action: suspend () -> Unit) {
        while (true) {
            try {
                action()
                return

            } catch (t: Throwable) {
                attempt += 1
                val errorDetails = t.message
                LoggerFactory.logger.error("Attempt $attempt/$retries failed: $contextMessage: $errorDetails")

                if (attempt >= retries) {
                    throw t
                }
                if (backoffDelayMs > 0) {
                    delay(backoffDelayMs)
                }

            }
        }
    }

}
