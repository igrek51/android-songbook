package igrek.songbook.mock

import igrek.songbook.info.UiInfoService
import igrek.songbook.inject.SingletonInject
import igrek.songbook.layout.LayoutController
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.room.RoomLobby
import igrek.songbook.songpreview.SongOpener
import igrek.songbook.songpreview.SongPreviewLayoutController
import org.mockito.Mockito

class SongOpenerMock : SongOpener(
    layoutController = SingletonInject { Mockito.mock(LayoutController::class.java) },
    songPreviewLayoutController = SingletonInject { Mockito.mock(SongPreviewLayoutController::class.java) },
    songsRepository = SingletonInject { Mockito.mock(SongsRepository::class.java) },
    uiInfoService = SingletonInject { Mockito.mock(UiInfoService::class.java) },
    roomLobby = SingletonInject { Mockito.mock(RoomLobby::class.java) },
)