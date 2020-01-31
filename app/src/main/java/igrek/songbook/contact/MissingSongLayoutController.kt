package igrek.songbook.contact

import android.view.View
import android.widget.Button
import android.widget.EditText
import igrek.songbook.R
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.UiResourceService
import igrek.songbook.info.errorcheck.SafeClickListener
import igrek.songbook.layout.LayoutController
import igrek.songbook.layout.MainLayout
import igrek.songbook.layout.dialog.ConfirmDialogBuilder
import igrek.songbook.system.SoftKeyboardService
import javax.inject.Inject

class MissingSongLayoutController : MainLayout {

    @Inject
    lateinit var layoutController: LayoutController
    @Inject
    lateinit var uiInfoService: UiInfoService
    @Inject
    lateinit var uiResourceService: UiResourceService
    @Inject
    lateinit var sendMessageService: SendMessageService
    @Inject
    lateinit var softKeyboardService: SoftKeyboardService

    private var missingSongMessageEdit: EditText? = null

    init {
        DaggerIoc.factoryComponent.inject(this)
    }

    override fun showLayout(layout: View) {
        missingSongMessageEdit = layout.findViewById(R.id.missingSongMessageEdit)

        layout.findViewById<Button>(R.id.contactSendButton)?.setOnClickListener(SafeClickListener {
            sendMessage()
        })
    }

    override fun getLayoutResourceId(): Int {
        return R.layout.screen_contact_missing_song
    }

    override fun onBackClicked() {
        layoutController.showLastSongSelectionLayout()
    }

    override fun onLayoutExit() {
        softKeyboardService.hideSoftKeyboard()
    }

    private fun sendMessage() {
        val message = missingSongMessageEdit?.text?.toString()

        if (message.isNullOrBlank()) {
            uiInfoService.showToast(uiResourceService.resString(R.string.fill_in_all_fields))
            return
        }

        if (!message.matches(Regex(".+-.+"))) {
            uiInfoService.showToast(uiResourceService.resString(R.string.missing_song_title_invalid_format))
            return
        }

        val subject = uiResourceService.resString(R.string.contact_subject_missing_song)

        ConfirmDialogBuilder().confirmAction(R.string.confirm_send_contact) {
            sendMessageService.sendContactMessage(message, origin = MessageOrigin.MISSING_SONG,
                    subject = subject)
        }
    }

}
