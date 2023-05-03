package igrek.songbook.cast

import android.view.View
import android.widget.ImageButton
import com.google.android.material.textfield.TextInputLayout
import igrek.songbook.R
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.errorcheck.SafeClickListener
import igrek.songbook.info.errorcheck.UiErrorHandler
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.layout.InflatedLayout
import igrek.songbook.layout.contextmenu.ContextMenuBuilder
import igrek.songbook.layout.dialog.ConfirmDialogBuilder
import igrek.songbook.layout.list.StringListView
import igrek.songbook.system.ClipboardManager
import kotlinx.coroutines.*

@OptIn(DelicateCoroutinesApi::class)
class SongCastLobbyLayout(
    uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
    songCastService: LazyInject<SongCastService> = appFactory.songCastService,
    clipboardManager: LazyInject<ClipboardManager> = appFactory.clipboardManager,
) : InflatedLayout(
    _layoutResourceId = R.layout.screen_cast_lobby
) {
    private val uiInfoService by LazyExtractor(uiInfoService)
    private val songCastService by LazyExtractor(songCastService)
    private val clipboardManager by LazyExtractor(clipboardManager)

    private var roomCodeInput: TextInputLayout? = null
    private var membersListView: StringListView? = null

    override fun showLayout(layout: View) {
        super.showLayout(layout)

        roomCodeInput = layout.findViewById<TextInputLayout?>(R.id.roomCodeInput)?.also {
            it.editText?.setText(songCastService.sessionShortId.orEmpty())
            it.editText?.setOnClickListener {
                copySessionCode()
            }
            it.setEndIconOnClickListener {
                copySessionCode()
            }
        }

        layout.findViewById<ImageButton>(R.id.moreActionsButton)
            ?.setOnClickListener(SafeClickListener {
                showMoreActions()
            })

        membersListView = layout.findViewById<StringListView>(R.id.membersListView)?.also {
            it.onClickCallback = { }
            it.items = listOf()
            it.emptyView = layout.findViewById(R.id.emptyListTextView)
        }

        updateSessionDetails()
        refreshSessionDetails()
    }

    private fun refreshSessionDetails() {
        GlobalScope.launch {
            val result = songCastService.getSessionDetailsAsync().await()
            result.fold(onSuccess = {
                GlobalScope.launch(Dispatchers.Main) {
                    updateSessionDetails()
                }
            }, onFailure = { e ->
                UiErrorHandler().handleError(e, R.string.error_communication_breakdown)
            })
        }
    }

    private fun updateSessionDetails() {
        roomCodeInput?.editText?.setText(songCastService.sessionShortId ?: "")

        val items = songCastService.members.map { member ->
            member.name
        }
        membersListView?.items = items
    }

    private fun copySessionCode() {
        clipboardManager.copyToSystemClipboard(songCastService.sessionShortId.orEmpty())
        uiInfoService.showInfo(R.string.songcast_code_copied)
    }

    private fun showMoreActions() {
        ContextMenuBuilder().showContextMenu(
            mutableListOf(
                ContextMenuBuilder.Action(R.string.songcast_exit_room) {
                    leaveRoomConfirm()
                },
            )
        )
    }

    private fun leaveRoomConfirm() {
        ConfirmDialogBuilder().confirmAction(R.string.songcast_confirm_leave_room) {
            GlobalScope.launch {
                exitRoom()
            }
        }
    }

    private suspend fun exitRoom() {
        uiInfoService.showInfo(R.string.songcast_you_left_the_room)
        layoutController.showLayout(SongCastLayout::class)
        val result = songCastService.dropSessionAsync().await()
        result.fold(onSuccess = {
        }, onFailure = { e ->
            UiErrorHandler().handleError(e, R.string.error_communication_breakdown)
        })
    }

    override fun onBackClicked() {
        when (songCastService.isInRoom()) {
            true -> leaveRoomConfirm()
            else -> super.onBackClicked()
        }
    }

}