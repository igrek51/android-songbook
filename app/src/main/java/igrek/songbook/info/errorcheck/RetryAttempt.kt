package igrek.songbook.info.errorcheck

import igrek.songbook.info.logger.LoggerFactory

class RetryAttempt(
    private val retries: Int,
    private val contextMessage: String,
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

            }
        }
    }

}
