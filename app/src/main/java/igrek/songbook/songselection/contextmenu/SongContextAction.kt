package igrek.songbook.songselection.contextmenu

import igrek.songbook.persistence.general.model.Song

class SongContextAction(
    val displayNameResId: Int,
    val availableCondition: (Song) -> Boolean,
    val executor: (Song) -> Unit
) {

    var displayName: String? = null

}