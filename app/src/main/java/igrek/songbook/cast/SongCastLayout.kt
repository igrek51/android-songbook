package igrek.songbook.cast

import android.view.View
import android.widget.Button
import igrek.songbook.R
import igrek.songbook.info.UiInfoService
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.layout.InflatedLayout
import kotlinx.coroutines.*
import com.google.android.material.textfield.TextInputLayout
import igrek.songbook.admin.ApiResponseError
import igrek.songbook.info.errorcheck.LocalizedError
import igrek.songbook.info.errorcheck.SafeClickListener
import igrek.songbook.info.errorcheck.UiErrorHandler
import igrek.songbook.info.errorcheck.safeExecute

@OptIn(DelicateCoroutinesApi::class)
class SongCastLayout(
    uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
    songCastService: LazyInject<SongCastService> = appFactory.songCastService,
) : InflatedLayout(
    _layoutResourceId = R.layout.screen_cast
) {
    private val uiInfoService by LazyExtractor(uiInfoService)
    private val songCastService by LazyExtractor(songCastService)

    private var nameInput: TextInputLayout? = null
    private var roomCodeInput: TextInputLayout? = null

    override fun showLayout(layout: View) {
        super.showLayout(layout)

        nameInput = layout.findViewById(R.id.nameInput)
        if (nameInput?.editText?.text?.isEmpty() == true) {
            val newName = AnimalNameFeeder().generateName()
            nameInput?.editText?.setText(newName)
        }
        roomCodeInput = layout.findViewById(R.id.roomCodeInput)

        layout.findViewById<Button>(R.id.createNewRoomButton)
            ?.setOnClickListener {
                GlobalScope.launch {
                    safeExecute {
                        createRoom()
                    }
                }
            }

        layout.findViewById<Button>(R.id.joinRoomButton)
            ?.setOnClickListener(SafeClickListener {
                GlobalScope.launch {
                    safeExecute {
                        joinRoom()
                    }
                }
            })
    }

    private fun getMemberName(): String {
        return nameInput?.editText?.text.toString().takeIf { it.isNotBlank() }
            ?: AnimalNameFeeder().generateName()
    }

    private suspend fun createRoom() {
        uiInfoService.showInfo(R.string.songcast_creating_room, indefinite = true)
        val result = songCastService.createSessionAsync(getMemberName()).await()
        result.fold(onSuccess = { _: CastSessionJoined ->
            layoutController.showLayout(SongCastLobbyLayout::class) {
                GlobalScope.launch(Dispatchers.Main) {
                    uiInfoService.showInfo(R.string.songcast_room_created)
                }
            }
        }, onFailure = { e ->
            UiErrorHandler().handleError(e, R.string.error_communication_breakdown)
        })
    }

    private suspend fun joinRoom() {
        val roomCode = roomCodeInput?.editText?.text.toString().trim()
        if (roomCode.isBlank()) {
            throw LocalizedError(R.string.songcast_room_code_empty)
        }
        if (!Regex("[0-9]{6}").matches(roomCode)) {
            throw LocalizedError(R.string.songcast_room_code_invalid)
        }

        uiInfoService.showInfo(R.string.songcast_joining_room, indefinite = true)
        val result = songCastService.joinSessionAsync(roomCode, getMemberName()).await()
        result.fold(onSuccess = { _: CastSessionJoined ->
            layoutController.showLayout(SongCastLobbyLayout::class) {
                GlobalScope.launch(Dispatchers.Main) {
                    uiInfoService.showInfo(R.string.songcast_room_joined)
                }
            }
        }, onFailure = { e ->
            if (e is ApiResponseError) {
                if (e.response.code() == 404) {
                    throw LocalizedError(R.string.songcast_room_not_found)
                }
            }
            UiErrorHandler().handleError(e, R.string.error_communication_breakdown)
        })
    }

}