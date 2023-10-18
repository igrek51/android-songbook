package igrek.songbook.custom.sync

import igrek.songbook.persistence.general.model.Song
import igrek.songbook.persistence.user.custom.CustomSong
import igrek.songbook.secret.ShaHasher
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class SongHasher {
    private val jsonSerializer = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
        isLenient = false
        allowStructuredMapKeys = true
        prettyPrint = false
        useArrayPolymorphism = false
    }

    fun hashLocalSongs(localSongs: List<CustomSong>): String {
        val hashableSongs: List<HashableCustomSongDto> = localSongs
            .map { HashableCustomSongDto.fromCustomSong(it) }
            .sortedBy { it.id }
        val dto = HashableCustomSongsDto(hashableSongs)
        val json = jsonSerializer.encodeToString(HashableCustomSongsDto.serializer(), dto)
        return ShaHasher().singleHash(json)
    }

    fun hashSong(song: Song): String {
        val dto = TitledSongDto(
            title = song.title,
            artist = song.artist,
            content = song.content ?: "",
            chordsNotationId = song.chordsNotation.id,
        )
        val json = jsonSerializer.encodeToString(TitledSongDto.serializer(), dto)
        return ShaHasher().singleHash(json)
    }

    fun stdSongContentHash(song: CustomSong): String {
        val parts: List<String> = listOf(
            song.title,
            song.artist ?: "",
            song.content,
            song.chordsNotationN.id.toString(),
        )
        return ShaHasher().singleHash(parts.joinToString("\n")).lowercase()
    }
}

@Serializable
data class HashableCustomSongsDto(
    var songs: List<HashableCustomSongDto> = emptyList()
)

@Serializable
data class HashableCustomSongDto(
    var id: String,
    var title: String,
    var artist: String,
    var content: String,
    var chordsNotationId: Long,
) {
    companion object {
        fun fromCustomSong(song: CustomSong): HashableCustomSongDto = HashableCustomSongDto(
            id = song.id,
            title = song.title,
            artist = song.categoryName.orEmpty(),
            content = song.content,
            chordsNotationId = song.chordsNotationN.id,
        )
    }
}

@Serializable
data class TitledSongDto(
    var title: String,
    var artist: String?,
    var content: String,
    var chordsNotationId: Long,
)
