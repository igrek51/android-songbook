package igrek.songbook.mock

import androidx.appcompat.app.AppCompatActivity
import igrek.songbook.info.UiResourceService
import org.mockito.Mockito

class UiResourceServiceMock : UiResourceService(
        Mockito.mock(AppCompatActivity::class.java),
) {

    override fun resString(resourceId: Int): String {
        return resourceId.toString()
    }

    override fun resString(resourceId: Int, vararg args: Any?): String {
        return resourceId.toString() + args.joinToString()
    }

}
