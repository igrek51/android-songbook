package igrek.songbook.domain.lyrics;

public class LyricsFragment {
	
	private float x;
	private String text;
	private LyricsTextType type;
	
	public LyricsFragment(float x, String text, LyricsTextType type) {
		this.x = x;
		this.text = text;
		this.type = type;
	}
	
	public float getX() {
		return x;
	}
	
	public void setX(float x) {
		this.x = x;
	}
	
	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	public LyricsTextType getType() {
		return type;
	}
	
	public void setType(LyricsTextType type) {
		this.type = type;
	}
	
	@Override
	public String toString() {
		return text;
	}
}

