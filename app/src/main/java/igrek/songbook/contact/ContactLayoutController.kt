package igrek.songbook.contact

import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import igrek.songbook.R
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.UiResourceService
import igrek.songbook.info.errorcheck.SafeClickListener
import igrek.songbook.layout.LayoutController
import igrek.songbook.layout.LayoutState
import igrek.songbook.layout.MainLayout
import igrek.songbook.layout.dialog.ConfirmDialogBuilder
import igrek.songbook.layout.navigation.NavigationMenuController
import igrek.songbook.system.SoftKeyboardService
import javax.inject.Inject

class ContactLayoutController : MainLayout {

    @Inject
    lateinit var layoutController: LayoutController
    @Inject
    lateinit var uiInfoService: UiInfoService
    @Inject
    lateinit var uiResourceService: UiResourceService
    @Inject
    lateinit var activity: AppCompatActivity
    @Inject
    lateinit var navigationMenuController: NavigationMenuController
    @Inject
    lateinit var sendFeedbackService: SendFeedbackService
    @Inject
    lateinit var softKeyboardService: SoftKeyboardService

    private var contactSubjectEdit: EditText? = null
    private var contactMessageEdit: EditText? = null
    private var contactAuthorEdit: EditText? = null

    init {
        DaggerIoc.factoryComponent.inject(this)
    }

    override fun showLayout(layout: View) {
        contactSubjectEdit = layout.findViewById(R.id.contactSubjectEdit)
        contactMessageEdit = layout.findViewById(R.id.contactMessageEdit)
        contactAuthorEdit = layout.findViewById(R.id.contactAuthorEdit)

        val contactSendButton = layout.findViewById<Button>(R.id.contactSendButton)
        contactSendButton.setOnClickListener(SafeClickListener {
            sendContactMessage()
        })
    }

    override fun getLayoutState(): LayoutState {
        return LayoutState.CONTACT
    }

    override fun getLayoutResourceId(): Int {
        return R.layout.screen_contact
    }

    override fun onBackClicked() {
        layoutController.showLastSongSelectionLayout()
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
            sendFeedbackService.sendFeedback(message, author, subject)
        }
    }

    private fun setSubject(subject: String) {
        contactSubjectEdit!!.setText(subject)
    }

    private fun setMessage(message: String?) {
        contactMessageEdit!!.setText(message)
    }

    fun prepareCustomSongPublishing(songTitle: String, customCategoryName: String?, songContent: String?) {
        val subjectPrefix = uiResourceService.resString(R.string.contact_subject_publishing_song)
        val fullTitle: String = if (customCategoryName.isNullOrEmpty()) {
            songTitle
        } else {
            "$songTitle - $customCategoryName"
        }
        setSubject("$subjectPrefix: $fullTitle")
        setMessage(songContent)
    }

    fun prepareMissingSongRequest() {
        val subject = uiResourceService.resString(R.string.contact_subject_missing_song)
        val message = uiResourceService.resString(R.string.contact_message_missing_song)
        setSubject(subject)
        setMessage(message)
    }
}
