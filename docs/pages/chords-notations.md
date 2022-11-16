# Chords Notations

**Chords Notation** is the note naming convention in music.

*Songbook* supports several chords notations:

-   **English** - 
    used in most countries.

    Names for the 12 notes:

    `A`, `A#` or **`Bb`**, **`B`**, `C`, `C#` or `Db`, `D`, `D#` or `Eb`, `E`, `F`, `F#` or `Gb`, `G`, `G#` or `Ab`

    Minor chords are denoted by appending `m` to the main note name (eg. `Em`).

-   **German (#/b)** - 
    used in Austria, Czech Republic, Germany, Denmark, Estonia, Finland, Hungary, Norway, Poland, Serbia, Slovakia, Slovenia, Sweden.

    Names for the 12 notes:

    `A`, `A#` or **`B`**, **`H`**, `C`, `C#` or `Db`, `D`, `D#` or `Eb`, `E`, `F`, `F#` or `Gb`, `G`, `G#` or `Ab`

    Minor chords are denoted by lowercase note name (eg. `e` is *E minor*).

-   **German (is/es)** -
    alternative **German** notation.

    Names for the 12 notes:

    `A`, `Ais` or **`B`**, **`H`**, `C`, `Cis` or `Des`, `D`, `Dis` or `Es`, `E`, `F`, `Fis` or `Ges`, `G`, `Gis` or `As`

    Minor chords are denoted by lowercase note name.

-   **Dutch** -
    used in Netherlands, Indonesia.

    Names for the 12 notes:

    `A`, `Ais` or **`Bes`**, **`B`**, `C`, `Cis` or `Des`, `D`, `Dis` or `Es`, `E`, `F`, `Fis` or `Ges`, `G`, `Gis` or `As`

    Minor chords are denoted by appending `m` to the main note name.

-   **Japanese** -
    used in Japan.

    Names for the 12 notes:

    `I`, `Ei-i` or `Hen-ro`, `Ro`, `Ha`, `Ei-ha` or `Hen-ni`, `Ni`, `Ei-ni` or `Hen-ho`, `Ho`, `He`, `Ei-he` or `Hen-to`, `To`, `Ei-to` or `Hen-i`

    Minor chords are denoted by appending `m` to the main note name.

-   **Solfege** - 
    used in Italy, France, Spain, Romania, Russia, Latin America, Greece, Israel, Turkey, Latvia.

    Names for the 12 notes:
    
    `La`, `La#` or `Sib`, `Si`, `Do`, `Do#` or `Reb`, `Re`, `Re#` or `Mib`, `Mi`, `Fa`, `Fa#` or `Solb`, `Sol`, `Sol#` or `Lab`

    Minor chords are denoted by appending `m` to the main note name.


You can pick your favourite chords notation in the Settings.

Default chords notation had already been set automatically depending on your system language
since some chords notations are used only in particular countries.

!!! note
    Name **`B`** in music can denote completely different notes depending on the chords notation.
    According to **English** notation `B` is the note being 1 semitone lower than `C`,
    while according to **German** notation it's the sound that is 2 semitones lower than `C` (so it's English `Bb`).
    Thus, it's extremely important to specify the appropriate **chords notation** before interpreting chords.

!!! note
    **German** chords notation changes note names to lowercase in case of a minor chord,
    eg. `d` (instead of English `Dm`).
    Songbook takes that into account too.

!!! question "What's the difference between C# or Db?"
    Basically, it depends on the **key** you're working on.
    In music, you don't mix **sharps** and **flats** in one song.
    Also, you avoid using the same note letter (with different **modifiers**) more than once
    in the same scale in classical theory.

    For instance, **F major** key consists of notes:
    `F`, `G`, `A`, `Bb` (or `A#`), `C`, `D`, `E`.

    It makes sense to write it with **flats** `F, G, A, Bb, C, D, E`,
    so each letter occurs only once.

    If you write it with **sharps** `F, G, A, A#, C, D, E`,
    you end up with `A` occurring twice (in 2 variants) and no `B` at all. It seems to be more wrong.

    Thus, it depends on the **key** context.
    Sometimes it's better to call it `C#` (eg. in **D major** key)
    and sometimes it makes more sense to call it `Db` (eg. in **Ab / G# major** key).

    *Songbook* takes that into account when displaying chords, once it detects a **key**.
