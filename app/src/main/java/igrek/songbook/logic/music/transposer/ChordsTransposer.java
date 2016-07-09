package igrek.songbook.logic.music.transposer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import igrek.songbook.output.Output;

public class ChordsTransposer {

    private final String soundNames[] = {
            "c", "c#", "d", "d#", "e", "f", "f#", "g", "g#", "a", "b", "h", //mollowe
            "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "B", "H" //durowe
    };

    private final String chordsDelimiters[] = {
            " ", "-", "(", ")", "/", ","
    };

    private final int MAX_LENGTH_ANALYZE = 2;

    private HashMap<String, Integer> soundNumbers;

    public ChordsTransposer() {
        soundNumbers = new HashMap<>();
        for (int i = 0; i < soundNames.length; i++) {
            soundNumbers.put(soundNames[i], i);
        }
    }

    /**
     * @param in zawartość pliku (z tekstem i akordami)
     * @param t liczba półtonów przesunięcia
     * @return przetransponowana zawartość (w tej samej postaci, co wejściowa)
     */
    public String transposeContent(String in, int t){

        StringBuilder out = new StringBuilder();

        StringBuilder chordsSection = new StringBuilder();
        boolean bracket = false;

        for (int i = 0; i < in.length(); i++) {

            char c = in.charAt(i);

            if (c=='[') {
                bracket = true;
                out.append(c);
            } else if (c == ']') {
                bracket = false;
                //transpozycja całej aktualnej sekcji akordów
                String transposedChords = transposeChords(chordsSection.toString(), t);
                out.append(transposedChords);

                out.append(c);

                chordsSection.delete(0, chordsSection.length());
            } else { //zwykły znak
                if(bracket){
                    chordsSection.append(c); //przepisanie do bufora sekcji akordów
                }else{
                    out.append(c);
                }
            }
        }


        return out.toString();
    }

    private Integer getChordNumber(String chord) {
        return soundNumbers.get(chord);
    }

    private String getChordName(int chordNr) {
        if (chordNr < 0 || chordNr >= soundNames.length) return null;
        return soundNames[chordNr];
    }

    /**
     * @param in sekcja akordów
     * @param t liczba półtonów przesunięcia
     * @return przetransponowana sekcja akordów
     */
    public String transposeChords(String in, int t) {
        StringBuilder out = new StringBuilder();

        //podział na poszczególne akordy
        List<StringWithDelimiter> chords = splitWithDelimiters(in, chordsDelimiters);

        for (StringWithDelimiter chord : chords) {
            String transposedChord = transposeChord(chord.str, t);
            out.append(transposedChord);
            out.append(chord.delimiter); //dopisanie tego samego separatora, który rozdzielił akord
        }

        return out.toString();
    }

    /**
     * @param chord akord formatu: C, C#, c, c#, Cmaj7, c7, c#7, Cadd9, Csus
     * @param t liczba półtonów przesunięcia
     * @return przetransponowany akord
     */
    private String transposeChord(String chord, int t) {

        if(chord.isEmpty()) return chord;

        //rozpoznanie akordu
        Integer chordNumber = getChordNumber(chord);
        String suffix = ""; //znaki dopisane do akordu, np. Cmaj7 (maj7)
        if (chordNumber == null) { //nie rozpoznano akordu podstawowego (bez dopisków)
            //próba rozpoznania akordu formatu: C#maj7, akord + [literki] + [liczba]
            //rozpoznawanie coraz krótszych podciągów
            for (int l = Math.min(MAX_LENGTH_ANALYZE, chord.length() - 1); l >= 1; l--) {
                String chordCut = chord.substring(0, l);
                suffix = chord.substring(l);
                chordNumber = getChordNumber(chordCut);
                if (chordNumber != null) break; //rozpoznano akord z dopisanymi znakami (literami lub liczbami)
            }
            if (chordNumber == null) { //nie rozpoznano żadnego akordu
                Output.warn("Transpozycja: Nie rozpoznano akordu: " + chord);
                return chord;
            }
        }

        //transpozycja - przesunięcie o półtony
        if (chordNumber < 12) { //mollowe: 0 - 11
            chordNumber = (chordNumber + t) % 12;
            if (chordNumber < 0) chordNumber += 12;
        } else { //durowe: 12 - 23
            chordNumber = (chordNumber - 12 + t) % 12 + 12;
            if (chordNumber < 12) chordNumber += 12;
        }

        return getChordName(chordNumber) + suffix;
    }

    /**
     * @param in tekst zawierający separatory
     * @param delimiters tablica separatorów
     * @return lista podzielonych fragmentów tekstu z zapamiętanymi separatorami (lub bez, jeśli jest to ostatni element)
     */
    private List<StringWithDelimiter> splitWithDelimiters(String in, String[] delimiters) {
        List<StringWithDelimiter> splitted = new ArrayList<>();

        //znalezienie najwcześniejszego separatora
        String minDelimiter = null;
        Integer minDelimiterIndex = null;
        for (String delimiter : delimiters) {
            int firstIndex = in.indexOf(delimiter);
            if (firstIndex != -1) {
                if (minDelimiterIndex == null || firstIndex < minDelimiterIndex) {
                    minDelimiterIndex = firstIndex;
                    minDelimiter = delimiter;
                }
            }
        }

        if (minDelimiterIndex == null) { //brak separatora
            splitted.add(new StringWithDelimiter(in)); //ostatni fragment
            return splitted;
        } else {
            String before = in.substring(0, minDelimiterIndex);
            String after = in.substring(minDelimiterIndex + minDelimiter.length());
            splitted.add(new StringWithDelimiter(before, minDelimiter));
            //rekurencyjny podział
            splitted.addAll(splitWithDelimiters(after, delimiters));
        }

        return splitted;
    }

    private class StringWithDelimiter {
        public String str;
        public String delimiter = null;

        public StringWithDelimiter(String str, String delimiter) {
            this.str = str;
            this.delimiter = delimiter;
        }

        public StringWithDelimiter(String str) {
            this.str = str;
            this.delimiter = "";
        }
    }
}
