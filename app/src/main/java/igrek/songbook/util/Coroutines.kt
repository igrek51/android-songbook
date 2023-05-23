package igrek.songbook.util

import igrek.songbook.info.errorcheck.UiErrorHandler
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

private val exceptionHandler = CoroutineExceptionHandler { _, throwable: Throwable ->
    UiErrorHandler().handleError(throwable)
}
val mainScope = CoroutineScope(Dispatchers.Main + exceptionHandler)
val defaultScope = CoroutineScope(Dispatchers.Default + exceptionHandler)
