package igrek.songbook.persistence.user.custom

import igrek.songbook.persistence.general.model.Song
import igrek.songbook.persistence.general.model.SongNamespace
import igrek.songbook.persistence.general.model.SongStatus


internal class CustomSongMapper {

    fun customSongToSong(customSong: CustomSong): Song {
        return Song(
            id = customSong.id,
            title = customSong.title,
            categories = mutableListOf(),
            content = customSong.content,
            versionNumber = customSong.versionNumber,
            createTime = customSong.createTime,
            updateTime = customSong.updateTime,
            comment = customSong.comment,
            preferredKey = customSong.preferredKey,
            author = customSong.author,
            status = SongStatus.CUSTOM,
            customCategoryName = customSong.categoryName,
            language = customSong.language,
            metre = customSong.metre,
            scrollSpeed = customSong.scrollSpeed,
            initialDelay = customSong.initialDelay,
            chordsNotation = customSong.chordsNotationN,
            originalSongId = customSong.originalSongId,
            namespace = SongNamespace.Custom,
        )
    }

    fun songToCustomSong(song: Song): CustomSong {
        return CustomSong(
            id = song.id,
            title = song.title,
            categoryName = song.customCategoryName,
            content = song.content ?: "",
            versionNumber = song.versionNumber,
            createTime = song.createTime,
            updateTime = song.updateTime,
            comment = song.comment,
            preferredKey = song.preferredKey,
            metre = song.metre,
            author = song.author,
            language = song.language,
            scrollSpeed = song.scrollSpeed,
            initialDelay = song.initialDelay,
            chordsNotation = song.chordsNotation,
            originalSongId = song.originalSongId,
        )
    }

}
