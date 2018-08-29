package igrek.songbook.view.songpreview;

import igrek.songbook.domain.crd.CRDFragment;
import igrek.songbook.domain.crd.CRDLine;
import igrek.songbook.domain.crd.CRDModel;
import igrek.songbook.domain.crd.CRDTextType;
import igrek.songbook.view.songpreview.base.Align;
import igrek.songbook.view.songpreview.base.Font;

public class LyricsRenderer {
	
	private CanvasGraphics canvas;
	private CRDModel crdModel;
	private float w, h;
	
	LyricsRenderer(CanvasGraphics canvas, CRDModel crdModel) {
		this.canvas = canvas;
		this.crdModel = crdModel;
		w = canvas.getW();
		h = canvas.getH();
	}
	
	public void drawFileContent(float fontsize, float lineheight) {
		canvas.setFontSize(fontsize);
		canvas.setColor(0xffffff);
		
		if (crdModel != null) {
			for (CRDLine line : crdModel.getLines()) {
				drawTextLine(line, canvas.getScroll(), fontsize, lineheight);
			}
		}
	}
	
	private void drawTextLine(CRDLine line, float scroll, float fontsize, float lineheight) {
		float y = line.getY() * lineheight - scroll;
		if (y > h)
			return;
		if (y + lineheight < 0)
			return;
		
		// line wrapper on bottom layer
		if (line.getFragments().size() > 0) {
			CRDFragment lastFragment = line.getFragments().get(line.getFragments().size() - 1);
			if (lastFragment.getType() == CRDTextType.LINEWRAPPER) {
				canvas.setFont(Font.FONT_NORMAL);
				canvas.setColor(0xa0a0a0);
				canvas.drawText(lastFragment.getText(), w, y + 0.85f * lineheight, Align.RIGHT);
			}
		}
		
		for (CRDFragment fragment : line.getFragments()) {
			
			if (fragment.getType() == CRDTextType.REGULAR_TEXT) {
				canvas.setFont(Font.FONT_NORMAL);
				canvas.setColor(0xffffff);
				canvas.drawText(fragment.getText(), fragment.getX() * fontsize, y + lineheight, Align.LEFT);
			} else if (fragment.getType() == CRDTextType.CHORDS) {
				canvas.setFont(Font.FONT_BOLD);
				canvas.setColor(0xf00000);
				canvas.drawText(fragment.getText(), fragment.getX() * fontsize, y + lineheight, Align.LEFT);
			}
			
		}
	}
	
	public void drawScrollBar() {
		float scroll = canvas.getScroll();
		float maxScroll = canvas.getMaxScroll();
		float range = maxScroll + h;
		float top = scroll / range;
		float bottom = (scroll + h) / range;
		
		canvas.setColor(0xAEC3E0);
		canvas.drawLine(w - 1, top * h, w - 1, bottom * h);
	}
}
