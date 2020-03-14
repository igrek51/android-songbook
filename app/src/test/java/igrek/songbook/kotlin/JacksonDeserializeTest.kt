package igrek.songbook.kotlin

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import igrek.songbook.admin.antechamber.AllAntechamberSongsDto
import igrek.songbook.persistence.general.model.Song
import org.junit.Test

class JacksonDeserializeTest {

    @Test
    fun test_jackson_deserialize() {
        val json = """
{
  "songs": [
    {
      "author": null,
      "category_name": "band",
      "chords_notation": 1,
      "comment": null,
      "content": "something",
      "create_time": 1581893053000,
      "id": 5,
      "initial_delay": null,
      "language": null,
      "metre": null,
      "original_song_id": null,
      "preferred_key": null,
      "scroll_speed": null,
      "status": 3,
      "title": "a new song",
      "update_time": 1581893299000,
      "version_number": 1
    }
  ]
}
            """.trim()
        val mapper = jacksonObjectMapper()
        val allDtos: AllAntechamberSongsDto = mapper.readValue(json)
        val antechamberSongs: List<Song> = allDtos.toModel()

        assert(antechamberSongs.isNotEmpty())
    }

}
