# Chord Format Specification

*Songbook* uses simple text file format to interpret songs.
It contains the song's lyrics intertwined with the chords.
The chords are placed between square brackets `[` and `]`, eg. `[C7]`.

You **must** adhere to the formatting principles in order to create a valid song in *Songbook*.

## Formatting Rules
Try to stick to these following rules in order to keep songs consistent with others:

1.  Mark **chords** by surrounding them with **square brackets**, e.g.
    ```
    [C]So, so you think you can [D]tell
    ```

2.  Mix chords with the lyrics in the **same line**, placing them **just before the words** 
    where they occur in order to precisely indicate where a chord should be accentuated, e.g. 
    ```
    Never gonna [G]give you [A]up
    ```

    !!! failure "Don't"
        **Do not place the chords on a separate line above the text**.

    !!! note
        If you want to display chords above the lyrics,
        you can always change your [Displaying Style](./displaying-styles.md) in **Settings**.
        So don't force it on others, let others choose their favourite displaying style.
        Placing chords in the same line is extremely important, 
        because only then chords are precisely defined in correct place,
        regardless different **font styles** or **word wrapping**. 
        Don't worry about your favourite chords displaying style,
        *Songbook* will do all the magic for you.

3.  If some part of a song is **repeated** (e.g. **chorus**), **copy** it to all its occurrences. 
    When lyrics are **continuous**, it facilitates scrolling through the song and 
    then you will not have to scroll back every time to see the refrain chords. 

    !!! failure "Don't"
        Don't put any superfluous annotations or paragraphs
        like `Chorus`, `Verse`, `Title`, `Solo`, `Instrumental`. 
        Include only lyrics with chords.

4.  If a song has **difficult rhythm** with chords changing **frequently**,
    place the chords **between words** or even **inside the words** in order to indicate 
    the syllable with a chord accent, e.g
    ```
    [G]Hotel Cali[D]fornia
    ``` 
    In very simple cases, it is allowed to place the chords **at the end of line**:
    ```
    Smoke on the water [C G#]
    ```

5. You can use **dash** `-` to couple quickly changing chords: 
`D Dsus2-D-Dsus4` or use **parentheses** `()` to mark some chords **optional**: `[C G (G7)]`.

6.  Make sure you have chosen appropriate **Notation** for your chords
    (`Am` for **English** or `a` for **German**).
    Find out [**why specifying Chord Notation is so important**](./chord-notations.md)

7.  Use [**Reformat**](./chords-editor.md#tools) and [**Validate**](./chords-editor.md#tools)
    tools on your song before saving it, 
    especially when you are going to publish it.

## Valid chords
Please use the valid chord names inside brackets, so the *Songbook* can interpret them correctly.
A chord name usually consists of two parts:

- **main note**: eg. `C`, `C#`, `D`, `Db` etc.
- **chord variant**: eg. `add4`, `m`, `maj7`, `7`, etc. or empty (simple major chord)

Having that in mind, **E minor** chord, should be named `Em` (in **English** notation).
Valid chords are for instance `C G# A7 F#maj7`

!!! failure "Don't"
    Don't use `Fm#` name, the valid chord is `F#m` instead (F sharp minor).

!!! note
    In some chord notations (like **German**) minor chords are written with lowecase note names, eg. `d` (instead of **English** `Dm`).
    Also, valid note names depend on a selected chord notation, eg. **German** notation have `A B H C ...` notes, while in **English** it's called `A Bb B C ...`.

## Example
```
[C]Twinkle, twinkle, [F]little [C]star
[F]How I [C]wonder [G7]what you [C]are.

[C]Example [G#]text [C G# A7 F#maj7]
[C]So, so you think you can [D]tell
Smoke on the water [C G#]

Never gonna [G]give you [A]up
Never gonna [F#m]let you [Bm]down
Never gonna [G]run [A]around and [F#]desert [Bm]you
Never gonna [G]make you [A]cry
Never gonna [F#m]say good[Bm]bye
Never gonna [G]tell [A]a lie and [F#]hurt [Bm]you
```

## Utilities
For your needs you can use comments by placing them between braces `{` and `}`,
eg. `{capo on 2 fret}`, or `{x2}`.
However it's discouraged to use the comments in the public songs.
Try to get rid of them before publishing a song.

Find out more about [**Chords Editor**](./chords-editor.md), which has a lot of useful tools.

## Importing songs
In order to load a song to **My songs**,
you can import it from a **local file** on your device
or any **cloud drive** supported by your device (like **Google Drive**).

You can load either:

- **text file** (`.txt`),
- **PDF document** (`.pdf`) or
- **Google Docs** document.

!!! note
    When importing a **PDF** file, the raw text will be extracted.
    Keep in mind that the extracted text might look different compared to original PDF,
    esepcially if it has non-flat structures inside, like tables or fancy paragraph layout.

## Editing with external tool
If you prefer, you can use any external text editor to create and modify a song in a **text** file format.

The text file should be saved with `UTF-8` encoding.

It can have arbitrary extension, but the common practice is to use `.txt` extension.

## Displaying style benefits
Thanks to indicating precisely the place where the chord is accentuated (right before the word),
Songbook can interpret that information in different ways when displaying a chord.
You can choose how you want to display the chords in settings.

Given the following text file
```
[C]Twinkle, twinkle, [F]little [C]star
[F]How I [C]wonder [G7]what you [C]are.
```

you can choose how to present the chords on a song preview:

- **Chords inline (among the words)** (In *Songbook* chords are bold with a color accent)
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

- **Chords aligned to right (at the end of line)**
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

!!! info "Remember"
    When editing a song, don't put the chords above words, keep them in the same line (among the words).
    Don't choose for others.
    Let others pick their favourite displaying style.
