package igrek.songbook.util

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

fun waitUntil(
        retries: Int,
        delayMs: Long,
        predicate: () -> Boolean,
): Boolean {
    for (attempt in 0 until retries) {
        val result = predicate()
        if (result)
            return true

        runBlocking {
            delay(delayMs)
        }
    }
    return false
}
