package igrek.songbook.settings.homescreen

import igrek.songbook.R
import igrek.songbook.custom.CustomSongsListLayoutController
import igrek.songbook.layout.MainLayout
import igrek.songbook.playlist.PlaylistLayoutController
import igrek.songbook.songpreview.SongPreviewLayoutController
import igrek.songbook.songselection.favourite.FavouritesLayoutController
import igrek.songbook.songselection.history.OpenHistoryLayoutController
import igrek.songbook.songselection.latest.LatestSongsLayoutController
import igrek.songbook.songselection.search.SongSearchLayoutController
import igrek.songbook.songselection.top.TopSongsLayoutController
import igrek.songbook.songselection.tree.SongTreeLayoutController
import kotlin.reflect.KClass

enum class HomeScreenEnum(
    val id: Long,
    val layoutClass: KClass<out MainLayout>,
    val nameResId: Int,
) {

    TOP_SONGS(1, TopSongsLayoutController::class, R.string.home_screen_enum_top),

    SONG_TREE(2, SongTreeLayoutController::class, R.string.home_screen_enum_categories),

    SONG_SEARCH(3, SongSearchLayoutController::class, R.string.home_screen_enum_search),

    LATEST_SONGS(4, LatestSongsLayoutController::class, R.string.home_screen_enum_latest),

    PLAYLISTS(5, PlaylistLayoutController::class, R.string.home_screen_enum_playlists),

    CUSTOM_SONGS(6, CustomSongsListLayoutController::class, R.string.home_screen_enum_my_songs),

    FAVOURITES(7, FavouritesLayoutController::class, R.string.home_screen_enum_favourites),

    HISTORY(8, OpenHistoryLayoutController::class, R.string.home_screen_enum_history),

    LAST_SONG(9, SongPreviewLayoutController::class, R.string.home_screen_enum_last_song),

    ;

    companion object {
        val default = TOP_SONGS

        fun parseById(id: Long): HomeScreenEnum? {
            return values().firstOrNull { v -> v.id == id }
        }

        fun mustParseById(id: Long): HomeScreenEnum {
            return parseById(id) ?: default
        }
    }
}
