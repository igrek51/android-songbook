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

class PublishSongLayoutController : MainLayout {

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

    private var publishSongTitleEdit: EditText? = null
    private var publishSongArtistEdit: EditText? = null
    private var publishSongContentEdit: EditText? = null
    private var contactAuthorEdit: EditText? = null
    private var originalSongId: Long? = null

    init {
        DaggerIoc.factoryComponent.inject(this)
    }

    override fun showLayout(layout: View) {
        publishSongTitleEdit = layout.findViewById(R.id.publishSongTitleEdit)
        publishSongArtistEdit = layout.findViewById(R.id.publishSongArtistEdit)
        publishSongContentEdit = layout.findViewById(R.id.publishSongContentEdit)
        contactAuthorEdit = layout.findViewById(R.id.contactAuthorEdit)

        layout.findViewById<Button>(R.id.contactSendButton)?.setOnClickListener(SafeClickListener {
            sendMessage()
        })
    }

    override fun getLayoutResourceId(): Int {
        return R.layout.screen_contact_publish_song
    }

    override fun onBackClicked() {
        layoutController.showLastSongSelectionLayout()
    }

    override fun onLayoutExit() {
        softKeyboardService.hideSoftKeyboard()
    }

    private fun sendMessage() {
        val title = publishSongTitleEdit?.text?.toString()
        val category = publishSongArtistEdit?.text?.toString()
        val content = publishSongContentEdit?.text?.toString()
        val author = contactAuthorEdit?.text?.toString()

        if (content.isNullOrBlank() || title.isNullOrBlank()) {
            uiInfoService.showToast(uiResourceService.resString(R.string.fill_in_all_fields))
            return
        }

        val subjectPrefix = if (originalSongId != null) {
            uiResourceService.resString(R.string.contact_subject_song_amend)
        } else {
            uiResourceService.resString(R.string.contact_subject_publishing_song)
        }
        val fullTitle: String = if (category.isNullOrEmpty()) {
            title
        } else {
            "$title - $category"
        }
        val subject = "$subjectPrefix: $fullTitle"

        ConfirmDialogBuilder().confirmAction(R.string.confirm_send_contact) {
            sendMessageService.sendContactMessage(message = content, origin = MessageOrigin.SONG_PUBLISH,
                    category = category, title = title, author = author, subject = subject,
                    originalSongId = originalSongId)
        }
    }

    fun prepareFields(songTitle: String, customCategoryName: String?, songContent: String?, originalSongId: Long?) {
        publishSongTitleEdit?.setText(songTitle)
        publishSongArtistEdit?.setText(customCategoryName ?: "")
        publishSongContentEdit?.setText(songContent ?: "")
        this.originalSongId = originalSongId
    }

}
