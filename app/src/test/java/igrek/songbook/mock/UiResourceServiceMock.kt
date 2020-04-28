package igrek.songbook.mock

import androidx.appcompat.app.AppCompatActivity
import igrek.songbook.info.UiResourceService
import igrek.songbook.inject.SingletonInject
import org.mockito.Mockito

class UiResourceServiceMock : UiResourceService(
        activity = SingletonInject { Mockito.mock(AppCompatActivity::class.java) },
) {

    override fun resString(resourceId: Int): String {
        return resourceId.toString()
    }

    override fun resString(resourceId: Int, vararg args: Any?): String {
        return resourceId.toString() + args.joinToString()
    }

}
