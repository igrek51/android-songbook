package igrek.songbook.songpreview

import igrek.songbook.R
import igrek.songbook.info.UiInfoService
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
    uiResourceService: LazyInject<UiResourceService> = appFactory.uiResourceService,
    songContextMenuBuilder: LazyInject<SongContextMenuBuilder> = appFactory.songContextMenuBuilder,
    uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
) {
    private val uiResourceService by LazyExtractor(uiResourceService)
    private val songContextMenuBuilder by LazyExtractor(songContextMenuBuilder)
    private val uiInfoService by LazyExtractor(uiInfoService)

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
        val namespaceName = buildNamespaceName(song)

        val messageLines = mutableListOf<String>()
        messageLines.add(uiResourceService.resString(R.string.song_details, namespaceName, path, songTitle, categories, songVersion, modificationDate))
        if (!preferredKey.isNullOrEmpty())
            messageLines.add(uiResourceService.resString(R.string.song_details_preferred_key, preferredKey))
        if (!metre.isNullOrEmpty())
            messageLines.add(uiResourceService.resString(R.string.song_details_metre, metre))
        if (!comment.isNullOrEmpty())
            messageLines.add(uiResourceService.resString(R.string.song_details_comment, comment))

        val message = messageLines.joinToString(separator = "\n")
        uiInfoService.dialogThreeChoices(
                titleResId = R.string.song_details_title,
                message = message,
                positiveButton = R.string.action_info_ok, positiveAction = {},
                neutralButton = R.string.song_action_more, neutralAction = { showMoreActions(song) },
        )
    }

    private fun buildSongPath(song: Song): String {
        val namespaceName = buildNamespaceName(song)

        var displayCategories = song.displayCategories()
        if (displayCategories.isEmpty()) {
            displayCategories = song.customCategoryName.orEmpty()
        }

        return listOf(namespaceName, displayCategories, song.title)
                .filter { it.isNotEmpty() }
                .joinToString(" / ")
    }

    private fun buildNamespaceName(song: Song): String {
        val namespaceId = when (song.namespace) {
            SongNamespace.Public -> R.string.song_details_namespace_public
            SongNamespace.Custom -> R.string.song_details_namespace_custom
            SongNamespace.Antechamber -> R.string.song_details_namespace_antechamber
            SongNamespace.Ephemeral -> R.string.song_details_namespace_temporary
        }
        return uiResourceService.resString(namespaceId)
    }

    private fun getLastModificationDate(song: Song): String {
        val time = song.updateTime // in milliseconds
        val cal = Calendar.getInstance()
        cal.timeInMillis = time
        return modificationDateFormat.format(cal.time)
    }

    private fun showMoreActions(song: Song) {
        songContextMenuBuilder.showSongActions(song)
    }

}
