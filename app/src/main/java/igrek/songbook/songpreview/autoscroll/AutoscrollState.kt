package igrek.songbook.songpreview.autoscroll

enum class AutoscrollState {

    OFF,

    WAITING, // loading eye focus zone from the beginning of the song

    SCROLLING,

    ENDING, // winding up eye focus zone to the ending of the song

    NEXT_SONG_COUNTDOWN,

}
