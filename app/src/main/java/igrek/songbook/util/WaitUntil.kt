package igrek.songbook.util

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeoutException

fun waitUntilCondition(
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

suspend fun waitUntil(
    retries: Int = 10,
    delayMs: Long = 100,
    predicate: () -> Boolean,
): Boolean {
    for (attempt in 0 until retries) {
        val result = predicate()
        if (result)
            return true

        delay(delayMs)
    }
    throw TimeoutException("condition not met")
}
