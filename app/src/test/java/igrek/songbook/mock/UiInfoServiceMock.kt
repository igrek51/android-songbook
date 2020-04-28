package igrek.songbook.mock

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.UiResourceService
import igrek.songbook.inject.SingletonInject
import org.mockito.Mockito


class UiInfoServiceMock : UiInfoService(
        activity = SingletonInject { Mockito.mock(AppCompatActivity::class.java) },
        uiResourceService = SingletonInject { Mockito.mock(UiResourceService::class.java) },
) {
    override fun showActionInfo(info: String, view: View?, actionName: String?, action: (() -> Unit)?, color: Int?, snackbarLength: Int) {
        print(info)
    }

    override fun showInfo(info: String) {
        print(info)
    }

    override fun showInfo(infoRes: Int, vararg args: String) {
        print(infoRes)
    }

    override fun showInfoIndefinite(info: String) {
        print(info)
    }

    override fun showInfoIndefinite(infoRes: Int, vararg args: String) {
        print(infoRes)
    }

    override fun showInfoWithAction(info: String, actionName: String, actionCallback: (() -> Unit), snackbarLength: Int) {
        print(info)
    }

    override fun showInfoWithAction(info: String, actionNameRes: Int, actionCallback: (() -> Unit)) {
        print(info)
    }

    override fun showInfoWithAction(infoRes: Int, actionNameRes: Int, actionCallback: (() -> Unit)) {
        print(infoRes)
    }

    override fun showInfoWithActionIndefinite(infoRes: Int, actionNameRes: Int, actionCallback: (() -> Unit)) {
        print(infoRes)
    }

    override fun showToast(message: String) {
        print(message)
    }

    override fun showToast(messageRes: Int) {
        print(messageRes)
    }

    override fun showDialog(title: String, message: String) {
        print(title)
    }

    override fun showTooltip(infoRes: Int) {
        print(infoRes)
    }

    override fun resString(resourceId: Int, vararg args: Any?): String {
        return resourceId.toString() + args.joinToString()
    }

}
