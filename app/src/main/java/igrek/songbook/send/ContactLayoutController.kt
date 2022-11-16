package igrek.songbook.send

import android.view.View
import android.widget.Button
import android.widget.EditText
import igrek.songbook.R
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.UiResourceService
import igrek.songbook.info.analytics.AnalyticsLogger
import igrek.songbook.info.errorcheck.SafeClickListener
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.layout.LayoutController
import igrek.songbook.layout.MainLayout
import igrek.songbook.layout.dialog.ConfirmDialogBuilder
import igrek.songbook.system.SoftKeyboardService

class ContactLayoutController(
    layoutController: LazyInject<LayoutController> = appFactory.layoutController,
    uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
    uiResourceService: LazyInject<UiResourceService> = appFactory.uiResourceService,
    sendMessageService: LazyInject<SendMessageService> = appFactory.sendMessageService,
    softKeyboardService: LazyInject<SoftKeyboardService> = appFactory.softKeyboardService,
) : MainLayout {
    private val layoutController by LazyExtractor(layoutController)
    private val uiInfoService by LazyExtractor(uiInfoService)
    private val uiResourceService by LazyExtractor(uiResourceService)
    private val sendMessageService by LazyExtractor(sendMessageService)
    private val softKeyboardService by LazyExtractor(softKeyboardService)

    private var contactSubjectEdit: EditText? = null
    private var contactMessageEdit: EditText? = null
    private var contactAuthorEdit: EditText? = null

    override fun showLayout(layout: View) {
        contactSubjectEdit = layout.findViewById(R.id.contactSubjectEdit)
        contactMessageEdit = layout.findViewById(R.id.contactMessageEdit)
        contactAuthorEdit = layout.findViewById(R.id.contactAuthorEdit)

        val contactSendButton = layout.findViewById<Button>(R.id.contactSendButton)
        contactSendButton.setOnClickListener(SafeClickListener {
            sendContactMessage()
        })
    }

    override fun getLayoutResourceId(): Int {
        return R.layout.screen_contact
    }

    override fun onBackClicked() {
        layoutController.showPreviousLayoutOrQuit()
    }

    override fun onLayoutExit() {
        softKeyboardService.hideSoftKeyboard()
    }

    private fun sendContactMessage() {
        val message = contactMessageEdit!!.text.toString()
        val author = contactAuthorEdit!!.text.toString()
        val subject = contactSubjectEdit!!.text.toString()
        if (message.isEmpty()) {
            uiInfoService.showToast(uiResourceService.resString(R.string.contact_message_field_empty))
            return
        }
        ConfirmDialogBuilder().confirmAction(R.string.confirm_send_contact) {
            sendMessageService.sendContactMessage(
                message, origin = MessageOrigin.CONTACT_MESSAGE,
                author = author, subject = subject
            )
            AnalyticsLogger().logEventContactMessageSent()
        }
    }
}
