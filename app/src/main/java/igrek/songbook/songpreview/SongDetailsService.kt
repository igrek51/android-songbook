package igrek.songbook.songpreview

import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import igrek.songbook.R
import igrek.songbook.info.UiResourceService
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.persistence.general.model.Song
import igrek.songbook.persistence.general.model.SongNamespace
import igrek.songbook.songselection.contextmenu.SongContextMenuBuilder
import java.text.SimpleDateFormat
import java.util.*

class SongDetailsService(
        appCompatActivity: LazyInject<AppCompatActivity> = appFactory.appCompatActivity,
        uiResourceService: LazyInject<UiResourceService> = appFactory.uiResourceService,
        songContextMenuBuilder: LazyInject<SongContextMenuBuilder> = appFactory.songContextMenuBuilder,
) {
    private val activity by LazyExtractor(appCompatActivity)
    private val uiResourceService by LazyExtractor(uiResourceService)
    private val songContextMenuBuilder by LazyExtractor(songContextMenuBuilder)

    private val modificationDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)

    fun showSongDetails(song: Song) {
        val comment = song.comment
        val preferredKey = song.preferredKey
        val metre = song.metre
        val songTitle = song.title
        val categories = song.displayCategories()
        val songVersion = song.versionNumber.toString()
        val modificationDate = getLastModificationDate(song)
        val path = buildSongPath(song)

        val messageLines = mutableListOf<String>()
        messageLines.add(uiResourceService.resString(R.string.song_details, path, songTitle, categories, songVersion, modificationDate))
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

    private fun buildSongPath(song: Song): String {
        val namespaceId = when (song.namespace) {
            SongNamespace.Public -> R.string.song_details_namespace_public
            SongNamespace.Custom -> R.string.song_details_namespace_custom
            SongNamespace.Antechamber -> R.string.song_details_namespace_antechamber
        }
        val namespaceName = uiResourceService.resString(namespaceId)

        var displayCategories = song.displayCategories()
        if (displayCategories.isEmpty()) {
            displayCategories = song.customCategoryName.orEmpty()
        }

        return listOf(namespaceName, displayCategories, song.title)
                .filter { it.isNotEmpty() }
                .joinToString(" / ")
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
        if (!activity.isFinishing) {
            alertDialog.show()
        }
    }

    private fun showMoreActions(song: Song) {
        songContextMenuBuilder.showSongActions(song)
    }

}
