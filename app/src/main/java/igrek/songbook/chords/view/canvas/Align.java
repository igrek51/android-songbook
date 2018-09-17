package igrek.songbook.chords.view.canvas;

public class Align {
	public static final int DEFAULT = 0x000;
	//  Pozycja
	public static final int LEFT = 0x001;
	public static final int RIGHT = 0x002;
	public static final int HCENTER = 0x004;
	public static final int TOP = 0x010;
	public static final int BOTTOM = 0x020;
	public static final int VCENTER = 0x040;
	// mieszane
	public static final int CENTER = HCENTER | VCENTER;
	public static final int BOTTOM_LEFT = BOTTOM | LEFT;
	public static final int BOTTOM_RIGHT = BOTTOM | RIGHT;
	public static final int BOTTOM_HCENTER = BOTTOM | HCENTER;
	public static final int TOP_LEFT = TOP | LEFT;
	public static final int TOP_RIGHT = TOP | RIGHT;
	public static final int TOP_HCENTER = TOP | HCENTER;
	//  Rozmiar
	public static final int HADJUST = 0x100;
	public static final int VADJUST = 0x200;
	public static final int ADJUST = HADJUST | VADJUST;
}