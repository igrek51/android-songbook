package igrek.songbook.custom

import igrek.songbook.custom.sync.SongHasher
import igrek.songbook.persistence.user.custom.CustomSong
import igrek.songbook.settings.chordsnotation.ChordsNotation
import org.assertj.core.api.Assertions
import org.junit.Test

class SongHasherTest {

    @Test
    fun test_encodeDecodeSong() {

        var hash = SongHasher().stdSongContentHash(CustomSong(
            id = "",
            title = "Echoes",
            categoryName = "Pink Floyd",
            content = "[c# A c# A]\n[c# A E H C]\nążśźęćół",
            chordsNotation = ChordsNotation.mustParseById(1),
        ))
        Assertions.assertThat(hash).isEqualTo("4c15db58214cd2b66fd99f53587d470d0185fe92cc4a1c82d998a201a017808c")

        hash = SongHasher().stdSongContentHash(CustomSong(
            id = "",
            title = "Echoes",
            categoryName = null,
            content = "[c# A c# A]\n[c# A E H C]\nążśźęćół",
            chordsNotation = ChordsNotation.mustParseById(1),
        ))
        Assertions.assertThat(hash).isEqualTo("48e0dd6326f61fcc08412c47be7293f7ec797e73080f2c3c531749a3d7c237da")
    }
}