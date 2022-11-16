package igrek.songbook.persistence.user.custom

import igrek.songbook.persistence.general.model.Song

data class CustomCategory(
    val name: String,
    val songs: MutableList<Song> = mutableListOf()
)