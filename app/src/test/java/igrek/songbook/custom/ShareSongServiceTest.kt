package igrek.songbook.custom

import igrek.songbook.inject.SingletonInject
import igrek.songbook.mock.SongOpenerMock
import igrek.songbook.persistence.general.model.Category
import igrek.songbook.persistence.general.model.CategoryType
import igrek.songbook.persistence.general.model.Song
import igrek.songbook.persistence.general.model.SongStatus
import igrek.songbook.settings.chordsnotation.ChordsNotation
import org.assertj.core.api.Assertions
import org.junit.Test

class ShareSongServiceTest {

    @Test
    fun test_encodeDecodeSong() {
        val shareSongEncoder = ShareSongService(songOpener = SingletonInject { SongOpenerMock() })
        val song = Song(
            id = 1,
            title = "Epitafium dla Włodzimierza Wysockiego",
            categories = mutableListOf(
                Category(1, type = CategoryType.ARTIST, name = "Jacek Kaczmarski")
            ),
            customCategoryName = "Kaczmarski",
            status = SongStatus.PUBLISHED,
            chordsNotation = ChordsNotation.ENGLISH,
            content = """
Do piekła! Do piekła! Do piekła! [a e]
Nie mam czasu na przejażdżki wiedźmo wściekła! [a C G G]
"""
        )

        val encoded = shareSongEncoder.encodeSong(song)
        Assertions.assertThat(encoded)
            .isEqualTo("H4sIAAAAAAAAAHWOIQ4CMRBFrzJU43BYICtIsAhATNoBhqWdTTubDSUYrsFNyNrei+IwuP/z8/L+3SjrlczcrDpWPHLvwV0RtuUpLrNnirm2WxLbMp3ETI2VoBS0IvuwFOiY2vLECfzLOwQ67MOGCTx6sBlTDwGhi5kuWEZXxpZhYHLl7QWG8rI/6AIaaCr+FfdJxS9Q6494q/412uwxppa/61miSxtRVJZg5rPHB0tkCkzcAAAA")

        val decodedBack = shareSongEncoder.decodeSong(encoded)
        Assertions.assertThat(decodedBack.title).isEqualTo("Epitafium dla Włodzimierza Wysockiego")
        Assertions.assertThat(decodedBack.customCategoryName).isEqualTo("Kaczmarski")
        Assertions.assertThat(decodedBack.content).isEqualTo(
            """
Do piekła! Do piekła! Do piekła! [a e]
Nie mam czasu na przejażdżki wiedźmo wściekła! [a C G G]
"""
        )
        Assertions.assertThat(decodedBack.chordsNotation).isEqualTo(ChordsNotation.ENGLISH)
    }

    @Test
    fun test_marshalUnmarshal() {
        val shareSongEncoder = ShareSongService(songOpener = SingletonInject { SongOpenerMock() })
        val song = Song(
            id = 1,
            title = "Epitafium dla Włodzimierza Wysockiego",
            categories = mutableListOf(
                Category(1, type = CategoryType.ARTIST, name = "Jacek Kaczmarski")
            ),
            customCategoryName = "Kaczmarski",
            status = SongStatus.PUBLISHED,
            chordsNotation = ChordsNotation.ENGLISH,
            content = """
Do piekła! Do piekła! Do piekła! [a e]
Nie mam czasu na przejażdżki wiedźmo wściekła! [a C G G]
""",
        )

        val marshaled = shareSongEncoder.marshal(song)
        Assertions.assertThat(marshaled)
            .isEqualTo("{\"title\":\"Epitafium dla Włodzimierza Wysockiego\",\"content\":\"\\nDo piekła! Do piekła! Do piekła! [a e]\\nNie mam czasu na przejażdżki wiedźmo wściekła! [a C G G]\\n\",\"customCategory\":\"Kaczmarski\",\"chordsNotation\":3}")

        val decodedBack = shareSongEncoder.unmarshal(marshaled)
        Assertions.assertThat(decodedBack.title).isEqualTo("Epitafium dla Włodzimierza Wysockiego")
        Assertions.assertThat(decodedBack.customCategoryName).isEqualTo("Kaczmarski")
        Assertions.assertThat(decodedBack.content).isEqualTo(
            """
Do piekła! Do piekła! Do piekła! [a e]
Nie mam czasu na przejażdżki wiedźmo wściekła! [a C G G]
"""
        )
        Assertions.assertThat(decodedBack.chordsNotation).isEqualTo(ChordsNotation.ENGLISH)
    }
}