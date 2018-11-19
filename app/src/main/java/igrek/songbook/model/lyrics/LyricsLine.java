package igrek.songbook.model.lyrics;

import com.google.common.base.Joiner;

import java.util.ArrayList;
import java.util.List;

public class LyricsLine {
	
	private int y;
	
	private List<LyricsFragment> fragments = new ArrayList<>();
	
	public LyricsLine() {
	}
	
	public void addFragment(LyricsFragment fragment) {
		fragments.add(fragment);
	}
	
	public List<LyricsFragment> getFragments() {
		return fragments;
	}
	
	public int getY() {
		return y;
	}
	
	public void setY(int y) {
		this.y = y;
	}
	
	@Override
	public String toString() {
		return Joiner.on(" ").join(fragments);
	}
}
