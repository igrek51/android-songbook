package igrek.songbook.send

import android.view.View
import android.widget.Button
import android.widget.EditText
import igrek.songbook.R
import igrek.songbook.admin.antechamber.AntechamberService
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.UiResourceService
import igrek.songbook.info.analytics.AnalyticsLogger
import igrek.songbook.info.errorcheck.SafeClickListener
import igrek.songbook.info.logger.LoggerFactory.logger
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.layout.LayoutController
import igrek.songbook.layout.MainLayout
import igrek.songbook.layout.dialog.ConfirmDialogBuilder
import igrek.songbook.persistence.general.model.Song
import igrek.songbook.system.SoftKeyboardService
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@OptIn(DelicateCoroutinesApi::class)
class PublishSongLayoutController(
        layoutController: LazyInject<LayoutController> = appFactory.layoutController,
        uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
        uiResourceService: LazyInject<UiResourceService> = appFactory.uiResourceService,
        sendMessageService: LazyInject<SendMessageService> = appFactory.sendMessageService,
        softKeyboardService: LazyInject<SoftKeyboardService> = appFactory.softKeyboardService,
        antechamberService: LazyInject<AntechamberService> = appFactory.antechamberService,
) : MainLayout {
    private val layoutController by LazyExtractor(layoutController)
    private val uiInfoService by LazyExtractor(uiInfoService)
    private val uiResourceService by LazyExtractor(uiResourceService)
    private val sendMessageService by LazyExtractor(sendMessageService)
    private val softKeyboardService by LazyExtractor(softKeyboardService)
    private val antechamberService by LazyExtractor(antechamberService)

    private var publishSongTitleEdit: EditText? = null
    private var publishSongArtistEdit: EditText? = null
    private var publishSongContentEdit: EditText? = null
    private var contactAuthorEdit: EditText? = null
    private var originalSongId: Long? = null
    private var publishSong: Song? = null

    override fun showLayout(layout: View) {
        publishSongTitleEdit = layout.findViewById(R.id.publishSongTitleEdit)
        publishSongArtistEdit = layout.findViewById(R.id.publishSongArtistEdit)
        publishSongContentEdit = layout.findViewById(R.id.publishSongContentEdit)
        contactAuthorEdit = layout.findViewById(R.id.contactAuthorEdit)

        publishSongContentEdit?.isEnabled = false

        layout.findViewById<Button>(R.id.contactSendButton)?.setOnClickListener(SafeClickListener {
            sendMessage()
        })
    }

    override fun getLayoutResourceId(): Int {
        return R.layout.screen_contact_publish_song
    }

    override fun onBackClicked() {
        layoutController.showPreviousLayoutOrQuit()
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
            uiInfoService.showToast(R.string.fill_in_all_fields)
            return
        }

        if (!isContentValid(content)) {
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

            publishSong?.let { publishSong ->
                GlobalScope.launch(Dispatchers.Main) {
                    val result = antechamberService.createAntechamberSong(publishSong).await()
                    result.fold(onSuccess = {
                        uiInfoService.showInfo(R.string.antechamber_new_song_sent)
                    }, onFailure = { e ->
                        logger.error(e)
                    })
                }

                AnalyticsLogger().logEventSongPublished(publishSong)
            }
        }
    }

    private fun isContentValid(content: String): Boolean {
        if ("[" !in content || "]" !in content) {
            uiInfoService.showToast(R.string.error_no_chords_marked_in_song)
            return false
        }
        return true
    }

    fun prepareFields(song: Song) {
        publishSong = song
        publishSongTitleEdit?.setText(song.title)
        publishSongArtistEdit?.setText(song.customCategoryName ?: "")
        publishSongContentEdit?.setText(song.content ?: "")
        this.originalSongId = song.originalSongId
    }

}
