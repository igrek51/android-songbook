package igrek.songbook.songselection.contextmenu

import android.app.Activity
import android.support.v7.app.AlertDialog
import igrek.songbook.R
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.UiResourceService
import igrek.songbook.info.errorcheck.SafeExecutor
import igrek.songbook.info.logger.Logger
import igrek.songbook.model.songsdb.Song
import igrek.songbook.model.songsdb.SongCategoryType
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
                    Logger.debug("edit")
                }),
                // REMOVE
                SongContextAction(R.string.action_song_remove, { song ->
                    song.category.type == SongCategoryType.CUSTOM
                }, { song ->
                    Logger.debug("remove")
                }),
                // SET_FAVORITE
                SongContextAction(R.string.action_song_set_favourite, { song ->
                    !favouriteSongsRepository.isSongFavourite(song)
                }, { song ->
                    Logger.debug("set favourite")
                }),
                // UNSET_FAVORITE
                SongContextAction(R.string.action_song_unset_favourite, { song ->
                    favouriteSongsRepository.isSongFavourite(song)
                }, { song ->
                    Logger.debug("unset favourite")
                }),
                // COPY
                SongContextAction(R.string.action_song_copy, { song ->
                    true
                }, { song ->
                    Logger.debug("copy")
                }),
                // AMEND
                SongContextAction(R.string.action_song_amend, { song ->
                    song.category.type != SongCategoryType.CUSTOM
                }, { song ->
                    Logger.debug("amend")
                }),
                // EXPORT
                SongContextAction(R.string.action_song_export, { song ->
                    song.category.type == SongCategoryType.CUSTOM
                }, { song ->
                    Logger.debug("export")
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