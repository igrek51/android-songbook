package igrek.songbook.view.songpreview;

import igrek.songbook.domain.lyrics.LyricsFragment;
import igrek.songbook.domain.lyrics.LyricsLine;
import igrek.songbook.domain.lyrics.LyricsModel;
import igrek.songbook.domain.lyrics.LyricsTextType;
import igrek.songbook.view.songpreview.canvas.Align;
import igrek.songbook.view.songpreview.canvas.Font;

public class LyricsRenderer {
	
	private SongPreview canvas;
	private LyricsModel lyricsModel;
	private float w, h;
	
	LyricsRenderer(SongPreview canvas, LyricsModel lyricsModel) {
		this.canvas = canvas;
		this.lyricsModel = lyricsModel;
		w = canvas.getW();
		h = canvas.getH();
	}
	
	/**
	 * @param fontsize   fontsize in pixels
	 * @param lineheight
	 */
	public void drawFileContent(float fontsize, float lineheight) {
		canvas.setFontSize(fontsize);
		canvas.setColor(0xffffff);
		
		if (lyricsModel != null) {
			for (LyricsLine line : lyricsModel.getLines()) {
				drawTextLine(line, canvas.getScroll(), fontsize, lineheight);
			}
		}
	}
	
	private void drawTextLine(LyricsLine line, float scroll, float fontsize, float lineheight) {
		float y = line.getY() * lineheight - scroll;
		if (y > h)
			return;
		if (y + lineheight < 0)
			return;
		
		// line wrapper on bottom layer
		if (line.getFragments().size() > 0) {
			LyricsFragment lastFragment = line.getFragments().get(line.getFragments().size() - 1);
			if (lastFragment.getType() == LyricsTextType.LINEWRAPPER) {
				canvas.setFont(Font.FONT_NORMAL);
				canvas.setColor(0xa0a0a0);
				canvas.drawText(lastFragment.getText(), w, y + 0.85f * lineheight, Align.RIGHT);
			}
		}
		
		for (LyricsFragment fragment : line.getFragments()) {
			
			if (fragment.getType() == LyricsTextType.REGULAR_TEXT) {
				canvas.setFont(Font.FONT_NORMAL);
				canvas.setColor(0xffffff);
				canvas.drawText(fragment.getText(), fragment.getX() * fontsize, y + lineheight, Align.LEFT);
			} else if (fragment.getType() == LyricsTextType.CHORDS) {
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
		int scrollWidth = 3;
		canvas.fillRect(w - scrollWidth, top * h, w, bottom * h);
	}
}
