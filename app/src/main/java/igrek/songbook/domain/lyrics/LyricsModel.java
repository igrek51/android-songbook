package igrek.songbook.domain.lyrics;

import com.google.common.base.Joiner;

import java.util.ArrayList;
import java.util.List;

public class LyricsModel {
	
	private List<LyricsLine> lines;
	
	public LyricsModel() {
		lines = new ArrayList<>();
	}
	
	public void addLines(List<LyricsLine> lines) {
		this.lines.addAll(lines);
	}
	
	public void addLine(LyricsLine line) {
		lines.add(line);
	}
	
	public List<LyricsLine> getLines() {
		return lines;
	}
	
	@Override
	public String toString() {
		return Joiner.on("\n").join(lines);
	}
}
