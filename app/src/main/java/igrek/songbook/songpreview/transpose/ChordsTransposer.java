package igrek.songbook.songpreview.transpose;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import igrek.songbook.info.logger.Logger;
import igrek.songbook.info.logger.LoggerFactory;
import igrek.songbook.settings.chordsnotation.ChordsNotation;

public class ChordsTransposer {
	
	/**
	 * supported chords formats:
	 * d, d#, D, D#, Dm, D#m, Dmaj7, D#maj7, d7, d#7, D#m7, D#7, Dadd9, Dsus
	 */
	private final String germanSoundNames[] = {
			// minor
			"c", "c#", "d", "d#", "e", "f", "f#", "g", "g#", "a", "b", "h",
			// major
			"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "B", "H",
			// american notation (major or minor with prefix)
			//"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "Bb", "B",
	};
	
	private final String englishSoundNames[] = {
			// minor
			"Cm", "C#m", "Dm", "D#m", "Em", "Fm", "F#m", "Gm", "G#m", "Am", "Bbm", "Bm",
			// major
			"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "Bb", "B",
	};
	
	private final String chordsDelimiters[] = {
			" ", "-", "(", ")", "/", ",", "\n"
	};
	
	private final int MAX_LENGTH_ANALYZE = 2;
	
	private final Comparator<String> lengthComparator = (lhs, rhs) -> {
		if (rhs.length() != lhs.length()) {
			return rhs.length() - lhs.length();
		} else {
			return lhs.compareTo(rhs);
		}
	};
	
	private Logger logger = LoggerFactory.getLogger();
	/**
	 * map: chord name (prefix) -> chord number
	 */
	private Map<String, Integer> soundNumbers;
	private ChordsNotation chordsNotation;
	
	public ChordsTransposer(ChordsNotation chordsNotation) {
		this.chordsNotation = chordsNotation;
		// keys sorted by length descending
		soundNumbers = new TreeMap<>(lengthComparator);
		for (int i = 0; i < germanSoundNames.length; i++) {
			soundNumbers.put(germanSoundNames[i], i);
		}
	}
	
	/**
	 * @param in file content with lyrics and chords
	 * @param t  shift semitones count
	 * @return content transposed by semitones (the same format as input)
	 */
	public String transposeContent(String in, int t) {
		StringBuilder out = new StringBuilder();
		StringBuilder chordsSection = new StringBuilder();
		boolean bracket = false;
		
		for (int i = 0; i < in.length(); i++) {
			char c = in.charAt(i);
			if (c == '[') {
				bracket = true;
				out.append(c);
			} else if (c == ']') {
				bracket = false;
				// transpose whole chords section
				String transposedChords = transposeChords(chordsSection.toString(), t);
				out.append(transposedChords);
				out.append(c);
				chordsSection.delete(0, chordsSection.length());
			} else { // the regular character
				if (bracket) {
					chordsSection.append(c); // move to chords section buffer
				} else {
					out.append(c);
				}
			}
		}
		
		return out.toString();
	}
	
	private Integer getChordNumber(String chord) {
		return soundNumbers.get(chord);
	}
	
	private String[] getChordsNotationSoundNames() {
		switch (chordsNotation) {
			case ENGLISH:
				return englishSoundNames;
			case GERMAN:
			default:
				return germanSoundNames;
		}
	}
	
	private String getChordName(int chordNr) {
		String[] soundNames = getChordsNotationSoundNames();
		if (chordNr < 0 || chordNr >= soundNames.length)
			return null;
		return soundNames[chordNr];
	}
	
	/**
	 * @param in chords section
	 * @param t  shift semitones count
	 * @return chords section transposed by semitones
	 */
	private String transposeChords(String in, int t) {
		StringBuilder out = new StringBuilder();
		
		//podział na poszczególne akordy
		List<StringWithDelimiter> chords = splitWithDelimiters(in, chordsDelimiters);
		
		for (StringWithDelimiter chord : chords) {
			String transposedChord = transposeChord(chord.str, t);
			out.append(transposedChord);
			out.append(chord.delimiter); // append the same delimiter which was splitting a chord
		}
		
		return out.toString();
	}
	
	/**
	 * @param chord chord in format: C, C#, c, c#, Cmaj7, c7, c#7, Cadd9, Csus
	 * @param t     shift semitones count
	 * @return chord transposed by semitones
	 */
	private String transposeChord(String chord, int t) {
		
		if (chord.trim().isEmpty())
			return chord;
		
		// chord recognition
		Integer chordNumber = getChordNumber(chord);
		String suffix = ""; // characters appended to a chords, e.g. Cmaj7 (maj7)
		if (chordNumber == null) { // basic chord not recognized (without suffixes)
			// attempt to recognize complex chord (with prefixes): C#maj7, akord + [letters] + [number]
			// recognition shorter and shorter substrings
			for (int l = Math.min(MAX_LENGTH_ANALYZE, chord.length() - 1); l >= 1; l--) {
				String chordCut = chord.substring(0, l);
				suffix = chord.substring(l);
				chordNumber = getChordNumber(chordCut);
				if (chordNumber != null)
					break; // a chord with suffix has been recognized
			}
			if (chordNumber == null) { // a chord was not recognized
				logger.warn("Transpose: Chord not recognized [" + chord.length() + "]: " + chord);
				return chord;
			}
		}
		
		// transpose by semitones
		int family = getChordFamilyIndex(chordNumber);
		chordNumber = chordNumber + t;
		// restore the original chord family
		while (getChordFamilyIndex(chordNumber) > family)
			chordNumber -= 12;
		while (chordNumber < 0 || getChordFamilyIndex(chordNumber) < family)
			chordNumber += 12;
		
		return getChordName(chordNumber) + suffix;
	}
	
	private int getChordFamilyIndex(int chordNumber) {
		return chordNumber / 12;
	}
	
	/**
	 * @param in         text with separators
	 * @param delimiters delimiters (separators) table
	 * @return a list of splitted text fragments with delimiters stored (or without if it's the last part)
	 */
	private List<StringWithDelimiter> splitWithDelimiters(String in, String[] delimiters) {
		List<StringWithDelimiter> splitted = new ArrayList<>();
		
		// find a first delimiter
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
		
		if (minDelimiterIndex == null) { //no delimiter
			splitted.add(new StringWithDelimiter(in)); // the last fragment
			return splitted;
		} else {
			String before = in.substring(0, minDelimiterIndex);
			String after = in.substring(minDelimiterIndex + minDelimiter.length());
			splitted.add(new StringWithDelimiter(before, minDelimiter));
			// recursive split
			splitted.addAll(splitWithDelimiters(after, delimiters));
		}
		
		return splitted;
	}
	
}
