package igrek.songbook.domain.crdfile;

public class CRDChar {

	public String c;

	public float width;

	public CRDTextType type;

	public CRDChar(String c, float width, CRDTextType type) {
		this.c = c;
		this.width = width;
		this.type = type;
	}

	@Override
	public String toString() {
		return c;
	}
}
