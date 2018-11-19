package igrek.songbook.songpreview

import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import igrek.songbook.R
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.UiResourceService
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

    private val modificationDateFormat = SimpleDateFormat("yyyy-MM-dd")

    init {
        DaggerIoc.getFactoryComponent().inject(this)
    }

    fun showSongDetails(song: Song) {
        val comment = song.comment
        val preferredKey = song.preferredKey
        val songTitle = song.title
        val category = song.category.displayName
        val songVersion = song.versionNumber.toString()
        val modificationDate = getLastModificationDate(song)

        var message = uiResourceService.resString(R.string.song_details, songTitle, category, songVersion, modificationDate)
        if (preferredKey != null)
            message += uiResourceService.resString(R.string.song_details_preferred_key, preferredKey)
        if (comment != null)
            message += uiResourceService.resString(R.string.song_details_comment, comment)

        val dialogTitle = uiResourceService.resString(R.string.song_details_title)

        val commentActionName = uiResourceService.resString(R.string.action_comment_song)
        val comemntAction = Runnable { addCommentToSong(song) }

        showDialogWithActions(dialogTitle, message, commentActionName, comemntAction)
    }

    private fun getLastModificationDate(song: Song): String {
        val time = song.updateTime // in milliseconds
        val cal = Calendar.getInstance()
        cal.timeInMillis = time
        return modificationDateFormat.format(cal.time)
    }

    private fun showDialogWithActions(title: String, message: String, neutralActionName: String, neutralAction: Runnable) {
        val alertBuilder = AlertDialog.Builder(activity)
        alertBuilder.setMessage(message)
        alertBuilder.setTitle(title)
        alertBuilder.setPositiveButton(uiResourceService.resString(R.string.action_info_ok)) { dialog, which -> }
        alertBuilder.setNeutralButton(neutralActionName) { dialog, which -> neutralAction.run() }
        alertBuilder.setCancelable(true)
        val alertDialog = alertBuilder.create()
        alertDialog.show()
    }

    fun addCommentToSong(song: Song) {
        // TODO redirect to feedback screen
    }

}
