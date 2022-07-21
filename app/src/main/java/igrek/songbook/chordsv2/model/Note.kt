package igrek.songbook.chordsv2.model


enum class NoteModifier {
    SHARP,
    NATURAL,
    FLAT,
}

enum class Note(val noteIndex: Int, modifier: NoteModifier) {

    C(0, NoteModifier.NATURAL),
    C_SHARP(1, NoteModifier.SHARP),
    D(2, NoteModifier.NATURAL),
    D_SHARP(3, NoteModifier.SHARP),
    E(4, NoteModifier.NATURAL),
    F(5, NoteModifier.NATURAL),
    F_SHARP(6, NoteModifier.SHARP),
    G(7, NoteModifier.NATURAL),
    G_SHARP(8, NoteModifier.SHARP),
    A(9, NoteModifier.NATURAL),
    A_SHARP(10, NoteModifier.SHARP),
    B(11, NoteModifier.NATURAL),

    D_FLAT(1, NoteModifier.FLAT),
    E_FLAT(3, NoteModifier.FLAT),
    G_FLAT(6, NoteModifier.FLAT),
    A_FLAT(8, NoteModifier.FLAT),
    B_FLAT(10, NoteModifier.FLAT),
    C_FLAT(11, NoteModifier.FLAT),
    E_SHARP(5, NoteModifier.SHARP),

}