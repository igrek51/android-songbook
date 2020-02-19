package igrek.songbook.songpreview

import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import igrek.songbook.R
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.UiResourceService
import igrek.songbook.layout.LayoutController
import igrek.songbook.persistence.general.model.Song
import igrek.songbook.persistence.general.model.SongNamespace
import igrek.songbook.songselection.contextmenu.SongContextMenuBuilder
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
    lateinit var songContextMenuBuilder: dagger.Lazy<SongContextMenuBuilder>

    private val modificationDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)

    init {
        DaggerIoc.factoryComponent.inject(this)
    }

    fun showSongDetails(song: Song) {
        val comment = song.comment
        val preferredKey = song.preferredKey
        val metre = song.metre
        val songTitle = song.title
        val category = song.displayCategories()
        val songVersion = song.versionNumber.toString()
        val modificationDate = getLastModificationDate(song)

        val messageLines = mutableListOf<String>()
        when (song.namespace) {
            SongNamespace.Public -> R.string.song_details_namespace_public
            SongNamespace.Custom -> R.string.song_details_namespace_custom
            else -> null
        }?.let {
            messageLines.add(uiResourceService.resString(it))
        }

        messageLines.add(uiResourceService.resString(R.string.song_details, songTitle, category, songVersion, modificationDate))
        if (!preferredKey.isNullOrEmpty())
            messageLines.add(uiResourceService.resString(R.string.song_details_preferred_key, preferredKey))
        if (!metre.isNullOrEmpty())
            messageLines.add(uiResourceService.resString(R.string.song_details_metre, metre))
        if (!comment.isNullOrEmpty())
            messageLines.add(uiResourceService.resString(R.string.song_details_comment, comment))

        val dialogTitle = uiResourceService.resString(R.string.song_details_title)

        val moreActionName = uiResourceService.resString(R.string.song_action_more)
        val moreAction = Runnable { showMoreActions(song) }

        val message = messageLines.joinToString(separator = "\n")
        showDialogWithActions(dialogTitle, message, moreActionName, moreAction, null, null)
    }

    private fun getLastModificationDate(song: Song): String {
        val time = song.updateTime // in milliseconds
        val cal = Calendar.getInstance()
        cal.timeInMillis = time
        return modificationDateFormat.format(cal.time)
    }

    private fun showDialogWithActions(title: String, message: String, neutralActionName: String?, neutralAction: Runnable?, negativeActionName: String?, negativeAction: Runnable?) {
        val alertBuilder = AlertDialog.Builder(activity)
                .setMessage(message)
                .setTitle(title)
                .setPositiveButton(uiResourceService.resString(R.string.action_info_ok)) { _, _ -> }
                .setCancelable(true)
        if (neutralAction != null)
            alertBuilder.setNeutralButton(neutralActionName) { _, _ -> neutralAction.run() }
        if (negativeAction != null)
            alertBuilder.setNegativeButton(negativeActionName) { _, _ -> negativeAction.run() }

        val alertDialog = alertBuilder.create()
        alertDialog.show()
    }

    private fun showMoreActions(song: Song) {
        songContextMenuBuilder.get().showSongActions(song)
    }

}
