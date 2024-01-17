package igrek.songbook.layout.dialog

import igrek.songbook.R
import igrek.songbook.info.UiInfoService
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory

class ConfirmDialogBuilder(
    uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
) {
    private val uiInfoService by LazyExtractor(uiInfoService)

    fun confirmAction(message: String, action: () -> Unit) {
        uiInfoService.dialogThreeChoices(
            titleResId = R.string.action_confirmation_title,
            message = message,
            negativeButton = R.string.action_cancel, negativeAction = {},
            positiveButton = R.string.action_info_ok, positiveAction = action,
        )
    }

    fun confirmAction(messageResId: Int, action: () -> Unit) {
        uiInfoService.dialogThreeChoices(
            titleResId = R.string.action_confirmation_title,
            messageResId = messageResId,
            negativeButton = R.string.action_cancel, negativeAction = {},
            positiveButton = R.string.action_info_ok, positiveAction = action,
        )
    }

}