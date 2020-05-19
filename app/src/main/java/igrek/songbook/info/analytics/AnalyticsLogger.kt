package igrek.songbook.info.analytics

import android.app.Activity
import android.os.Bundle
import android.provider.Settings
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.persistence.general.model.Song

class AnalyticsLogger(
        activity: LazyInject<Activity> = appFactory.activity,
) {
    private val activity by LazyExtractor(activity)

    private val firebaseAnalytics = Firebase.analytics

    fun logEventContactMessageSent() {
        logEvent("x_contact_message_sent", mapOf(
        ))
    }

    fun logEventSongPublished(song: Song) {
        logEvent("x_song_published", mapOf(
                "title" to song.title,
                "category" to song.customCategoryName,
                "language" to song.language,
        ))
    }

    fun logEventMissingSongRequested(name: String) {
        val (category, title) = extractArtistAndTitle(name)
        logEvent("x_missing_song_requested", mapOf(
                "category" to category,
                "title" to title,
        ))
    }

    fun logEventSongOpened(song: Song) {
        logEvent("x_song_opened", mapOf(
                "id" to song.id.toString(),
                "namespace" to song.namespace.toString(),
        ))
    }

    fun logEventSongFavourited(song: Song) {
        logEvent("x_song_favourited", mapOf(
                "id" to song.id.toString(),
                "namespace" to song.namespace.toString(),
        ))
    }

    private fun extractArtistAndTitle(songName: String): Pair<String, String> {
        val firstHyphen = songName.indexOfFirst { it == '-' }
        if (firstHyphen == -1) {
            return "" to songName.trim()
        }
        val artist = songName.take(firstHyphen)
        val title = songName.drop(firstHyphen + 1)
        return artist.trim() to title.trim()
    }

    private fun getDeviceId(): String? {
        return Settings.Secure.getString(activity.contentResolver, Settings.Secure.ANDROID_ID)
                ?: null
    }

    private fun logEvent(eventName: String, values: Map<String, String?>) {
        val bundle = Bundle()
        values.forEach { (key, value) ->
            if (value != null) {
                bundle.putString(key, value)
            }
        }
        firebaseAnalytics.logEvent(eventName, bundle)
    }
}