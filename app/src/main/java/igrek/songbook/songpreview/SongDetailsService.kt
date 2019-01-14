package igrek.songbook.songpreview

import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import igrek.songbook.R
import igrek.songbook.contact.ContactLayoutController
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.UiResourceService
import igrek.songbook.layout.LayoutController
import igrek.songbook.model.songsdb.Song
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class SongDetailsService {

    @Inject
    lateinit var activity: AppCompatActivity
    @Inject
    lateinit var uiInfoService: UiInfoService
    @Inject
    lateinit var uiResourceService: UiResourceService
    @Inject
    lateinit var layoutController: dagger.Lazy<LayoutController>
    @Inject
    lateinit var contactLayoutController: dagger.Lazy<ContactLayoutController>

    private val modificationDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)

    init {
        DaggerIoc.getFactoryComponent().inject(this)
    }

    fun showSongDetails(song: Song) {
        val comment = song.comment
        val preferredKey = song.preferredKey
        val metre = song.metre
        val songTitle = song.title
        val category = song.category.displayName
        val songVersion = song.versionNumber.toString()
        val modificationDate = getLastModificationDate(song)

        var message = uiResourceService.resString(R.string.song_details, songTitle, category, songVersion, modificationDate)
        if (preferredKey != null)
            message += "\n" + uiResourceService.resString(R.string.song_details_preferred_key, preferredKey)
        if (metre != null)
            message += "\n" + uiResourceService.resString(R.string.song_details_metre, metre)
        if (comment != null)
            message += "\n" + uiResourceService.resString(R.string.song_details_comment, comment)

        val dialogTitle = uiResourceService.resString(R.string.song_details_title)

        val amendActionName = uiResourceService.resString(R.string.action_amend_song)
        val amendAction = Runnable { amendSong(song) }

        val commentActionName = uiResourceService.resString(R.string.action_comment_song)
        val commentAction = Runnable { commentSong(song) }

        showDialogWithActions(dialogTitle, message, amendActionName, amendAction, commentActionName, commentAction)
    }

    private fun getLastModificationDate(song: Song): String {
        val time = song.updateTime // in milliseconds
        val cal = Calendar.getInstance()
        cal.timeInMillis = time
        return modificationDateFormat.format(cal.time)
    }

    private fun showDialogWithActions(title: String, message: String, neutralActionName: String, neutralAction: Runnable, negativeActionName: String, negativeAction: Runnable) {
        val alertBuilder = AlertDialog.Builder(activity)
                .setMessage(message)
                .setTitle(title)
                .setNeutralButton(neutralActionName) { _, _ -> neutralAction.run() }
                .setNegativeButton(negativeActionName) { _, _ -> negativeAction.run() }
                .setPositiveButton(uiResourceService.resString(R.string.action_info_ok)) { _, _ -> }
                .setCancelable(true)
        val alertDialog = alertBuilder.create()
        alertDialog.show()
    }

    private fun amendSong(song: Song) {
        layoutController.get().showContact()
        contactLayoutController.get().prepareSongAmend(song)
    }

    private fun commentSong(song: Song) {
        layoutController.get().showContact()
        contactLayoutController.get().prepareSongComment(song)
    }

}
