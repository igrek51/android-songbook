"""Tests for the ug2songbook conversion pipeline."""
from ug2songbook import _rendered_column, merge_chords, process_tab_block, ug_to_songbook


def test_rendered_column():
    """[ch] and [/ch] tags are invisible; their width is subtracted."""
    chord_line = "         [ch]Am[/ch]                          [ch]F[/ch]"
    assert _rendered_column(9, chord_line) == 9, "Am position unchanged (no preceding tags)"
    assert _rendered_column(46, chord_line) == 37, "F position subtracts [ch][/ch] width"


def test_merge_chords_simple():
    """Two chords: one mid-word, one on next word."""
    chord_line = "         [ch]Am[/ch]                          [ch]F[/ch]"
    lyric_line = "Czy kochałabyś mnie bardziej za dwie stówy?"
    result = merge_chords(chord_line, lyric_line)
    assert result == "Czy kocha[Am]łabyś mnie bardziej za dwie [F]stówy?", f"Got: {result}"


def test_merge_chords_end_of_word():
    """Chord at tail of word (preceding a space) moves to start of next word."""
    chord_line = "          [ch]C[/ch]                        [ch]G[/ch]"
    lyric_line = "Czy w tej cenie byś zrobiła to bez gumy?"
    result = merge_chords(chord_line, lyric_line)
    assert result == "Czy w tej [C]cenie byś zrobiła to bez [G]gumy?", f"Got: {result}"


def test_merge_chords_multi_colon():
    """Four chords across one line."""
    chord_line = "      [ch]Am[/ch]            [ch]F[/ch]                [ch]C[/ch]       [ch]G[/ch]"
    lyric_line = "Czy kochałabyś mnie bardziej za dwie stówy?"
    result = merge_chords(chord_line, lyric_line)
    assert result == "Czy ko[Am]chałabyś mnie [F]bardziej za dwie [C]stówy?  [G]", f"Got: {result}"


def test_merge_chords_eol():
    """Chord past lyric length becomes end-of-line group."""
    chord_line = "     [ch]Am[/ch]                     [ch]F[/ch]"
    lyric_line = "Na zegarek nie zerkała co minutę"
    result = merge_chords(chord_line, lyric_line)
    assert result == "Na ze[Am]garek nie zerkała co mi[F]nutę", f"Got: {result}"


def test_process_tab_block():
    """Full [tab] block processing preserves leading spaces."""
    inner = (
        "         [ch]Am[/ch]                          [ch]F[/ch]\n"
        "Czy kochałabyś mnie bardziej za dwie stówy?"
    )
    result = process_tab_block(inner)
    assert result == "Czy kocha[Am]łabyś mnie bardziej za dwie [F]stówy?", f"Got: {result}"


def test_full_pipeline_raw_input():
    """Full pipeline with the exact raw format from UG: section header + [tab] block."""
    raw = (
        "[Verse]\n"
        "[tab]         [ch]Am[/ch]                          [ch]F[/ch]\n"
        "Czy kochałabyś mnie bardziej za dwie stówy?[/tab]\n"
        "[tab]          [ch]C[/ch]                        [ch]G[/ch]\n"
        "Czy w tej cenie byś zrobiła to bez gumy?[/tab]\n"
        "[tab]     [ch]Am[/ch]                     [ch]F[/ch]\n"
        "Na zegarek nie zerkała co minutę[/tab]\n"
        "[tab]         [ch]C[/ch]                        [ch]G[/ch]\n"
        "Przy francuzie mi włożyła palec w dupę?[/tab]\n"
        "[tab]      [ch]Am[/ch]            [ch]F[/ch]                [ch]C[/ch]       [ch]G[/ch]\n"
        "Czy kochałabyś mnie bardziej za dwie stówy?[/tab]"
    )
    result = ug_to_songbook(raw)
    expected = (
        "Czy kocha[Am]łabyś mnie bardziej za dwie [F]stówy?\n"
        "Czy w tej [C]cenie byś zrobiła to bez [G]gumy?\n"
        "Na ze[Am]garek nie zerkała co mi[F]nutę\n"
        "Przy fran[C]cuzie mi włożyła palec w [G]dupę?\n"
        "Czy ko[Am]chałabyś mnie [F]bardziej za dwie [C]stówy?  [G]"
    )
    assert result == expected, f"\nGot:\n{result}\nExpected:\n{expected}"


def test_process_tab_block_with_cr():
    """Carriage return in chord line does not break alignment."""
    inner = (
        "         [ch]Am[/ch]                          [ch]F[/ch]\r\n"
        "Czy kochałabyś mnie bardziej za dwie stówy?"
    )
    result = process_tab_block(inner)
    assert result == "Czy kocha[Am]łabyś mnie bardziej za dwie [F]stówy?", f"Got: {result}"
