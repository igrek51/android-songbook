package igrek.songbook.info.errorcheck


import igrek.songbook.BuildConfig
import igrek.songbook.R
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory


class UiErrorHandler(
    uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
) {
    private val uiInfoService by LazyExtractor(uiInfoService)

    fun handleContextError(t: Throwable, vararg contextRedIds: Int) {
        val context = contextRedIds.joinToString(": ") { uiInfoService.resString(it) }
        handleError(t, context)
    }

    fun handleError(t: Throwable, contextResId: Int = R.string.error_occurred_s) {
        val err: String = when {
            t.message != null -> t.message
            else -> t::class.simpleName
        }.orEmpty()

        if (t is LocalizedError) {
            uiInfoService.showInfo(t.messageRes, indefinite = true)
        } else {
            LoggerFactory.logger.error(t)
            uiInfoService.showInfoAction(
                contextResId,
                err,
                indefinite = true,
                actionResId = R.string.error_details
            ) {
                showDetails(t, contextResId)
            }
        }
    }

    fun handleError(t: Throwable, context: String) {
        val err: String = when {
            t.message != null -> t.message
            else -> t::class.simpleName
        }.orEmpty()

        if (t is LocalizedError) {
            uiInfoService.showInfo(t.messageRes, indefinite = true)
        } else {
            LoggerFactory.logger.error(context, t)
            val errorMessage = "$context: $err"
            uiInfoService.showSnackbar(
                info = errorMessage,
                actionResId = R.string.error_details,
                indefinite = true,
            ) {
                showDetails(t, context)
            }
        }
    }

    private fun showDetails(t: Throwable, contextResId: Int) {
        val errorMessage = uiInfoService.resString(contextResId, formatErrorMessage(t))
        val message = when (BuildConfig.DEBUG) {
            true -> "${errorMessage}\nType: ${t::class.simpleName}"
            false -> errorMessage
        }
        uiInfoService.dialogThreeChoices(
            titleResId = R.string.error_occurred,
            message = message,
            positiveButton = R.string.action_info_ok, positiveAction = {},
            neutralButton = R.string.action_report_error, neutralAction = {
                appFactory.crashlyticsLogger.get().reportNonFatalError(t)
                uiInfoService.showToast(R.string.report_error_sent)
            },
        )
    }

    private fun showDetails(t: Throwable, context: String) {
        val errorMessage = "$context: ${formatErrorMessage(t)}"
        val message = when (BuildConfig.DEBUG) {
            true -> "${errorMessage}\nType: ${t::class.simpleName}"
            false -> errorMessage
        }
        uiInfoService.dialogThreeChoices(
            titleResId = R.string.error_occurred,
            message = message,
            positiveButton = R.string.action_info_ok, positiveAction = {},
            neutralButton = R.string.action_report_error, neutralAction = {
                appFactory.crashlyticsLogger.get().reportNonFatalError(t)
                uiInfoService.showToast(R.string.report_error_sent)
            },
        )
    }

    companion object {
        fun handleError(t: Throwable, contextResId: Int = R.string.error_occurred_s) {
            UiErrorHandler().handleError(t, contextResId)
        }
    }

}