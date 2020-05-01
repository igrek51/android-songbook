package igrek.songbook.mock

import androidx.appcompat.app.AppCompatActivity
import igrek.songbook.inject.SingletonInject
import igrek.songbook.system.ClipboardManager
import org.mockito.Mockito


class ClipboardManagerMock : ClipboardManager(
        activity = SingletonInject { Mockito.mock(AppCompatActivity::class.java) },
)
