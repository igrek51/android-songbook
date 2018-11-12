package igrek.songbook.layout.songpreview

import android.support.v7.app.AppCompatActivity
import igrek.songbook.R
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.domain.songsdb.Song
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.UiResourceService
import igrek.songbook.info.logger.LoggerFactory
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

    private val logger = LoggerFactory.getLogger()

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
        uiInfoService.showDialog(dialogTitle, message)
    }

    private fun getLastModificationDate(song: Song): String {
        val time = song.updateTime // in milliseconds
        val cal = Calendar.getInstance()
        cal.timeInMillis = time
        return modificationDateFormat.format(cal.time)
    }

}
