# Chord Format Specification

*Songbook* uses a simple text file format to represent songs.
It is **compatible with the widely-used Chord Pro format** --
any `.txt` file with chords in `[brackets]` placed inline with lyrics will work.

The chords are placed between square brackets `[` and `]`, e.g. `[C7]`.
Only chords get brackets; everything else is plain lyrics.

## Formatting Rules

### 1. Mark chords with square brackets

```
[C]So, so you think you can [D]tell
```

### 2. Place chords inline with lyrics, right before the word (or syllable) they accent

```
Never gonna [G]give you [A]up
```

The chord goes **immediately before** the word where the strum or accent falls.
This is the only rule that matters. Everything else follows from it.

!!! failure "Don't"
    **Do not place chords on a separate line above the lyrics.**
    If you have a source that looks like this:

    ```
           Am                     F
    When I find myself in times of trouble
    ```

    you must **merge** the chords into the lyrics at their column positions:

    ```
    When I [Am]find myself in times of [F]trouble
    ```

    Songbook can render chords above the lyrics at display time --
    see [Displaying Styles](./displaying-styles.md). But the source file must
    keep them inline to preserve exact positioning regardless of font styles or word wrapping.

### 3. Chord changes inside a word -- split at syllable boundaries

For precise accent placement, a chord can fall **mid-word**.
Insert the chord at the syllable boundary nearest the column position:

Source (chords above text):
```
    Am
beautiful
```
Result: `beau[Am]tiful`

Source (chords above text):
```
     G           F
Hallelujah, Hallelujah
```
Result: `Halle[G]lujah, Halle[F]lujah`

This is essential for songs with complex rhythms or when importing from
tab sites that use the line-above format.

### 4. No section headers, no annotations

!!! failure "Don't"
    Do **not** include labels like `[Verse]`, `[Chorus]`, `[Bridge]`,
    `[Solo]`, `[Intro]`, `[Outro]`, `[Instrumental]`, `[Refrain]`, `[x2]`.
    Include only lyrics with chords.

If a section repeats, **copy-paste it** instead of referring back.
This keeps the file scrollable without jumping around.

### 5. End-of-line chords (simple songs)

For very simple strumming patterns, chords may be grouped at the **end of the line**
(though it's encouraged to place them inline before the word they accent):

```
Smoke on the water [C G#]
```

### 6. Quick chord changes

Use a dash to chain fast changes: `D Dsus2-D-Dsus4`
Mark optional chords with parentheses: `[C G (G7)]`

### 7. Use the right notation

Use **English notation** (`Am`, `C#`, `F#m`) for broad compatibility.
See [Chord Notations](./chord-notations.md) for details.

### 8. Reformat and Validate before publishing

Use the built-in [Reformat and Validate tools](./chords-editor.md#tools)
to clean up spacing and catch invalid chords.

## Converting from "chords above lyrics" (Ultimate Guitar etc.)

If you have a tab written as two rows:

```
       Am                       F
When I find myself in times of trouble
```

Take each chord from the upper line and insert it **at its column position**
into the text line below. If a chord lands in the middle of a word, split
that word at the closest syllable boundary:

```
When I [Am]find myself in times of [F]trouble
```

(Am aligns with the `f` in "find" -- placed right before the word.
F aligns with the `t` in "trouble" -- also right before the word.)

## Valid chords

A chord name has two parts:

- **Main note** -- `C`, `C#` (or `Db`), `D`, `D#` (or `Eb`), `E`, `F`, `F#` (or `Gb`), `G`, `G#` (or `Ab`), `A`, `A#` (or `Bb`), `B`
- **Variant** -- `m`, `7`, `maj7`, `add9`, `sus4`, `dim`, `aug`, etc., or empty (major)

Valid examples: `C`, `G#`, `A7`, `F#maj7`, `Em`, `Bb`, `C#m`

!!! failure "Don't"
    `Fm#` is wrong. Write `F#m` instead (F sharp minor).

## Example

```
[C]Twinkle, twinkle, [F]little [C]star
[F]How I [C]wonder [G7]what you [C]are.

[C]Up a[F]bove the [C]world so [G7]high
[C]Like a [F]diamond [G7]in the [C]sky.

[C]Twinkle, twinkle, [F]little [C]star
[F]How I [C]wonder [G7]what you [C]are.
---
Never gonna [G]give you [A]up
Never gonna [G]let you [A]down
Never gonna [D]run around and [F#]desert [Bm]you
Never gonna [G]make you [A]cry
Never gonna [G]say good[A]bye
Never gonna [G]tell [A]a lie and [F#]hurt [Bm]you
```

## Comments

Place comments in curly braces for personal notes:

```
{capo on 2 fret}
{original key: Dm}
{x2}
```

Avoid publishing songs with comments.

Find out more about the [**Chords Editor**](./chords-editor.md), which has a lot of useful tools for editing and transforming songs.

## Importing songs

To load a song into **My songs**, you can import it from a **local file**
or any **cloud drive** supported by your device (like Google Drive).

Supported formats:

- **Text file** (`.txt`) -- native Songbook / Chord Pro format
- **PDF document** (`.pdf`) -- raw text will be extracted
- **Google Docs** document

!!! note
    When importing a **PDF** file, the raw text will be extracted.
    The extracted text may differ from the original PDF layout,
    especially if it contains tables or complex paragraph formatting.

## File format

- **Encoding**: UTF-8
- **Extension**: `.txt` (or any, but `.txt` is conventional)
- **Compatibility**: This format is a subset of **Chord Pro**, so files can be
  used with other Chord Pro-compatible tools and songbooks.

## Displaying style benefits

Because chords are tied to exact positions in the text, Songbook can
render them in multiple visual styles at display time.

Given the following source file:

```
[C]Twinkle, twinkle, [F]little [C]star
[F]How I [C]wonder [G7]what you [C]are.
```

You can choose how to present the chords:

- **Chords inline (among the words)** -- chords are bold with a color accent
    ```
    C Twinkle, twinkle, F little C star
    F How I C wonder G7 what you C are.
    ```

- **Chords above the lyrics (on a separate line)**
    ```
    C                F      C
    Twinkle, twinkle little star.
    F     C      G7       C
    How I wonder what you are.
    ```

- **Chords aligned to the right (at the end of line)**
    ```
    Twinkle, twinkle, little star         C F C
    How I wonder what you are.         F C G7 C
    ```

- **Lyrics alone (chords hidden)**
    ```
    Twinkle, twinkle, little star
    How I wonder what you are.
    ```

- **Chords alone (lyrics hidden)**
    ```
    C F C
    F C G7 C
    ```

See [Displaying Styles](./displaying-styles.md) for more details.

!!! info "Remember"
    Always keep chords inline (in the same line among the words) in the source file.
    Don't choose for others.
    Let listeners choose their favourite display style.
