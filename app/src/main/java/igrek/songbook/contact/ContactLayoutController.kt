package igrek.songbook.contact

import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import dagger.Lazy
import igrek.songbook.R
import igrek.songbook.activity.ActivityController
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.UiResourceService
import igrek.songbook.info.errorcheck.SafeClickListener
import igrek.songbook.layout.LayoutController
import igrek.songbook.layout.LayoutState
import igrek.songbook.layout.MainLayout
import igrek.songbook.layout.navigation.NavigationMenuController
import igrek.songbook.model.songsdb.Song
import igrek.songbook.system.SoftKeyboardService
import javax.inject.Inject

class ContactLayoutController : MainLayout {

    @Inject
    lateinit var activityController: Lazy<ActivityController>
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
        DaggerIoc.getFactoryComponent().inject(this)
    }

    override fun showLayout(layout: View) {
        // Toolbar
        val toolbar1 = layout.findViewById<Toolbar>(R.id.toolbar1)
        activity.setSupportActionBar(toolbar1)
        val actionBar = activity.supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false)
            actionBar.setDisplayShowHomeEnabled(false)
        }
        // navigation menu button
        val navMenuButton = layout.findViewById<ImageButton>(R.id.navMenuButton)
        navMenuButton.setOnClickListener { v -> navigationMenuController.navDrawerShow() }

        contactSubjectEdit = layout.findViewById(R.id.contactSubjectEdit)
        contactMessageEdit = layout.findViewById(R.id.contactMessageEdit)
        contactAuthorEdit = layout.findViewById(R.id.contactAuthorEdit)

        val contactSendButton = layout.findViewById<Button>(R.id.contactSendButton)
        contactSendButton.setOnClickListener(object : SafeClickListener() {
            override fun onClick() {
                sendContactMessage()
            }
        })
    }

    override fun getLayoutState(): LayoutState {
        return LayoutState.CONTACT
    }

    override fun getLayoutResourceId(): Int {
        return R.layout.contact
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
        if (message == null || message.isEmpty()) {
            uiInfoService.showToast(uiResourceService.resString(R.string.contact_message_field_empty))
            return
        }
        sendFeedbackService.sendFeedback(message, author, subject)
    }

    private fun setSubject(subject: String) {
        contactSubjectEdit!!.setText(subject)
    }

    fun prepareSongComment(song: Song) {
        val subjectPrefix = uiResourceService.resString(R.string.contact_subject_song_comment)
        setSubject(subjectPrefix + ": " + song.displayName())
        val dialogTitle = uiResourceService.resString(R.string.contact_subject_song_comment)
        val dialogMessage = uiResourceService.resString(R.string.contact_info_song_comment)
        uiInfoService.showDialog(dialogTitle, dialogMessage)
    }

    fun prepareCustomSongPublishing(songTitle: String, customCategoryName: String, songContent: String) {

    }
}
