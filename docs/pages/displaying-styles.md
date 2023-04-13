# Displaying Styles
Songbook allows to customize the style of displaying chords in a song.

Thanks to indicating precisely the place where the chord is accentuated (right before the word),
Songbook can interpret that information in different ways when displaying a chord.
You can choose how you want to display the chords in settings.

Given the following text file
```
[C]Twinkle, twinkle, [F]little [C]star
[F]How I [C]wonder [G7]what you [C]are.
```

you can choose how to present the chords on a song preview:

- **Chords inline (among the words)** (as original text, but chords are bold with a color accent)
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
