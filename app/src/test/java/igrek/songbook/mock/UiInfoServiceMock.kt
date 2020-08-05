package igrek.songbook.mock

import androidx.appcompat.app.AppCompatActivity
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.UiResourceService
import igrek.songbook.inject.SingletonInject
import org.mockito.Mockito


class UiInfoServiceMock : UiInfoService(
        activity = SingletonInject { Mockito.mock(AppCompatActivity::class.java) },
        uiResourceService = SingletonInject { Mockito.mock(UiResourceService::class.java) },
) {
    override fun showSnackbar(info: String, infoResId: Int, actionResId: Int, action: (() -> Unit)?, indefinite: Boolean) {
        print(info)
    }

    override fun showToast(message: String) {
        print(message)
    }

    override fun resString(resourceId: Int, vararg args: Any?): String {
        return resourceId.toString() + args.joinToString()
    }

}
