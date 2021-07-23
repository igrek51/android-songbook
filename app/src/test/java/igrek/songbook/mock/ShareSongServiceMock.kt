package igrek.songbook.mock

import androidx.appcompat.app.AppCompatActivity
import igrek.songbook.custom.share.ShareSongService
import igrek.songbook.inject.SingletonInject
import org.mockito.Mockito

class ShareSongServiceMock : ShareSongService(
    songOpener = SingletonInject { SongOpenerMock() },
    activity = SingletonInject { Mockito.mock(AppCompatActivity::class.java) },
)