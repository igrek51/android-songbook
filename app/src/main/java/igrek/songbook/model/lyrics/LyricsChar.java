package igrek.songbook.model.lyrics;

public class LyricsChar {
	
	public String c;
	
	public float width;
	
	public LyricsTextType type;
	
	public LyricsChar(String c, float width, LyricsTextType type) {
		this.c = c;
		this.width = width;
		this.type = type;
	}
	
	@Override
	public String toString() {
		return c;
	}
}
