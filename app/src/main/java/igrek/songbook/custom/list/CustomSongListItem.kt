package igrek.songbook.custom.list

import igrek.songbook.persistence.general.model.Song
import igrek.songbook.persistence.user.custom.CustomCategory

open class CustomSongListItem(
        val song: Song? = null,
        val customCategory: CustomCategory? = null
) {

    override fun toString(): String {
        return when {
            song != null -> song.displayName()
            customCategory != null -> """[${customCategory.name}]"""
            else -> ""
        }
    }

}
