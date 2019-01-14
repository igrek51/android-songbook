package igrek.songbook.songpreview.transpose;

import org.junit.Test;

import igrek.songbook.settings.chordsnotation.ChordsNotation;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class ChordsTransposerTest {
	
	private ChordsTransposer germanTransposer = new ChordsTransposer(ChordsNotation.GERMAN);
	private ChordsTransposer germanFisTransposer = new ChordsTransposer(ChordsNotation.GERMAN_IS);
	private ChordsTransposer englishTransposer = new ChordsTransposer(ChordsNotation.ENGLISH);
	private String transposed;
	
	@Test
	public void test_noTransposition() {
		String in = "a b c d [e f G G# B H]";
		assertThat(germanTransposer.transposeContent(in, 0)).isEqualTo(in);
		assertThat(germanTransposer.transposeContent(in, 12)).isEqualTo(in);
		assertThat(germanTransposer.transposeContent(in, -12)).isEqualTo(in);
	}
	
	@Test
	public void test_transposePlus1() {
		transposed = germanTransposer.transposeContent("a b c d [e f G G# B H]", 1);
		assertThat(transposed).isEqualTo("a b c d [f f# G# A H C]");
	}
	
	@Test
	public void test_transposeMinus1() {
		transposed = germanTransposer.transposeContent("a b c d [f f# G# A H C]", -1);
		assertThat(transposed).isEqualTo("a b c d [e f G G# B H]");
	}
	
	@Test
	public void test_englishChordsTranpose() {
		String in = "a b c d [e f G G# B H]";
		assertThat(englishTransposer.transposeContent(in, 0)).isEqualTo("a b c d [Em Fm G G# Bb B]");
		
		assertThat(englishTransposer.transposeContent(in, 1)).isEqualTo("a b c d [Fm F#m G# A B C]");
	}
	
	@Test
	public void test_germanFisChordsTranpose() {
		String in = "a b c d [e f G7 G# B H]";
		assertThat(germanFisTransposer.transposeContent(in, 0)).isEqualTo("a b c d [e f G7 Gis B H]");
		assertThat(germanFisTransposer.transposeContent(in, 1)).isEqualTo("a b c d [f fis Gis7 A H C]");
	}
}
