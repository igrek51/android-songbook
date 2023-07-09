package igrek.songbook.songpreview

import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.appFactory
import igrek.songbook.playlist.PlaylistService
import igrek.songbook.settings.preferences.SettingsState
import igrek.songbook.songselection.random.RandomSongOpener

class SongGestureController {

    private val playlistService: PlaylistService by LazyExtractor(appFactory.playlistService)
    private val songOpener: SongOpener by LazyExtractor(appFactory.songOpener)
    private val randomSongOpener: RandomSongOpener by LazyExtractor(appFactory.randomSongOpener)
    private val settingsState: SettingsState by LazyExtractor(appFactory.settingsState)

    fun goLeft(): Boolean {
        return playlistService.goToNextOrPrevious(-1)
    }

    fun goRight(): Boolean {
        return when {
            playlistService.goToNextOrPrevious(1) -> true
            settingsState.swipeToRandomizeAgain && songOpener.lastSongWasRandom -> {
                randomSongOpener.openRandomSong()
                true
            }
            else -> false
        }
    }
}