package igrek.songbook.util

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

suspend fun launchAndJoin(vararg blocks: suspend () -> Unit) = coroutineScope {
    val jobs = blocks.map {
        launch { it() }
    }
    jobs.joinAll()
}
