package igrek.songbook.songselection.contextmenu

import android.app.Activity
import android.support.v7.app.AlertDialog
import igrek.songbook.R
import igrek.songbook.contact.SendFeedbackService
import igrek.songbook.custom.CustomSongService
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.UiResourceService
import igrek.songbook.info.errorcheck.SafeExecutor
import igrek.songbook.persistence.songsdb.Song
import igrek.songbook.persistence.songsdb.SongCategoryType
import igrek.songbook.songselection.favourite.FavouriteSongsRepository
import igrek.songbook.system.cache.SimpleCache
import javax.inject.Inject


class SongContextMenuBuilder {

    @Inject
    lateinit var activity: Activity
    @Inject
    lateinit var uiResourceService: UiResourceService
    @Inject
    lateinit var favouriteSongsRepository: FavouriteSongsRepository
    @Inject
    lateinit var customSongService: CustomSongService
    @Inject
    lateinit var sendFeedbackService: SendFeedbackService

    private var allActions: SimpleCache<List<SongContextAction>> =
            SimpleCache { createAllActions() }

    init {
        DaggerIoc.getFactoryComponent().inject(this)
    }

    private fun createAllActions(): List<SongContextAction> {
        val actions = mutableListOf(
                // EDIT
                SongContextAction(R.string.action_song_edit, { song ->
                    song.category.type == SongCategoryType.CUSTOM
                }, { song ->
                    customSongService.showEditSongScreen(song)
                }),
                // REMOVE
                SongContextAction(R.string.action_song_remove, { song ->
                    song.category.type == SongCategoryType.CUSTOM
                }, { song ->
                    customSongService.removeSong(song)
                }),
                // SET_FAVORITE
                SongContextAction(R.string.action_song_set_favourite, { song ->
                    !favouriteSongsRepository.isSongFavourite(song)
                }, { song ->
                    favouriteSongsRepository.setSongFavourite(song)
                }),
                // UNSET_FAVORITE
                SongContextAction(R.string.action_song_unset_favourite, { song ->
                    favouriteSongsRepository.isSongFavourite(song)
                }, { song ->
                    favouriteSongsRepository.unsetSongFavourite(song)
                }),
                // COPY
                SongContextAction(R.string.action_song_copy, { song ->
                    true
                }, { song ->
                    customSongService.copySongAsCustom(song)
                }),
                // AMEND
                SongContextAction(R.string.action_song_amend, { song ->
                    song.category.type != SongCategoryType.CUSTOM
                }, { song ->
                    sendFeedbackService.amendSong(song)
                }),
                // ADD COMMENT
                SongContextAction(R.string.action_song_add_comment, { song ->
                    song.category.type != SongCategoryType.CUSTOM
                }, { song ->
                    sendFeedbackService.commentSong(song)
                })
        )

        actions.forEach { action -> action.displayName = uiResourceService.resString(action.displayNameResId) }
        return actions
    }

    fun showSongActions(song: Song) {
        val songActions = allActions.get()
                .filter { action -> action.availableCondition(song) }
        val actionNames = songActions.map { action -> action.displayName }.toTypedArray()

        val builder = AlertDialog.Builder(activity)
        builder.setTitle("Choose action")
        builder.setItems(actionNames) { _, item ->
            SafeExecutor().execute {
                songActions[item].executor(song)
            }
        }

        val alert = builder.create()
        alert.show()
    }

}