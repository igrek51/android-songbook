package igrek.songbook.domain.crdfile;

public class CRDFragment {
	
	private float x;
	private String text;
	private CRDTextType type;
	
	public CRDFragment(float x, String text, CRDTextType type) {
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
	
	public CRDTextType getType() {
		return type;
	}
	
	public void setType(CRDTextType type) {
		this.type = type;
	}
	
	@Override
	public String toString() {
		return text;
	}
}

