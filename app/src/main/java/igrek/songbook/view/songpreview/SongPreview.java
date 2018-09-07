package igrek.songbook.view.songpreview;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;

import java.util.List;

import javax.inject.Inject;

import dagger.Lazy;
import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.domain.lyrics.LyricsLine;
import igrek.songbook.domain.lyrics.LyricsModel;
import igrek.songbook.logger.Logger;
import igrek.songbook.logger.LoggerFactory;
import igrek.songbook.service.autoscroll.AutoscrollService;
import igrek.songbook.service.layout.songpreview.SongPreviewLayoutController;
import igrek.songbook.service.system.WindowManagerService;
import igrek.songbook.view.songpreview.canvas.BaseCanvasView;
import igrek.songbook.view.songpreview.quickmenu.QuickMenu;

public class SongPreview extends BaseCanvasView implements View.OnTouchListener {
	
	private final float EOF_SCROLL_RESERVE = 0.09f;
	private final float LINEHEIGHT_SCALE_FACTOR = 1.02f;
	private final float FONTSIZE_SCALE_FACTOR = 0.6f;
	private final float MIN_SCROLL_EVENT = 15f;
	
	@Inject
	Lazy<SongPreviewLayoutController> songPreviewController;
	@Inject
	Lazy<AutoscrollService> autoscroll;
	@Inject
	Lazy<QuickMenu> quickMenu;
	@Inject
	WindowManagerService windowManagerService;
	
	private LyricsModel lyricsModel;
	private float scroll;
	private float startScroll;
	private float fontsizeTmp;
	private Float pointersDst0;
	private Float fontsize0;
	private LyricsRenderer lyricsRenderer;
	protected float startTouchX = 0;
	protected float startTouchY = 0;
	protected long startTouchTime;
	private Logger logger = LoggerFactory.getLogger();
	
	public SongPreview(Context context) {
		super(context);
		DaggerIoc.getFactoryComponent().inject(this);
	}
	
	@Override
	public void reset() {
		super.reset();
		scroll = 0;
		startScroll = 0;
		pointersDst0 = null;
		fontsize0 = null;
		lyricsModel = null;
	}
	
	@Override
	public void init() {
		songPreviewController.get().onGraphicsInitializedEvent(w, h, paint);
	}
	
	@Override
	public void onRepaint() {
		// draw Background
		setColor(0x000000);
		clearScreen();
		
		if (this.lyricsRenderer != null) {
			lyricsRenderer.drawScrollBar();
			lyricsRenderer.drawFileContent(getFontsizePx(), getLineheightPx());
		}
		quickMenu.get().draw();
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getActionMasked()) {
			case MotionEvent.ACTION_DOWN:
				onTouchDown(event);
				break;
			case MotionEvent.ACTION_MOVE:
				onTouchMove(event);
				break;
			case MotionEvent.ACTION_UP:
				onTouchUp(event);
				break;
			case MotionEvent.ACTION_POINTER_DOWN:
				onTouchPointerDown(event);
				break;
			case MotionEvent.ACTION_POINTER_UP:
				onTouchPointerUp(event);
				break;
		}
		return false;
	}
	
	private void onTouchDown(MotionEvent event) {
		startTouchX = event.getX();
		startTouchY = event.getY();
		startTouchTime = System.currentTimeMillis();
		startScroll = scroll;
		pointersDst0 = null;
	}
	
	private void onTouchMove(MotionEvent event) {
		if (event.getPointerCount() >= 2) {
			// pinch to font scaling
			if (pointersDst0 != null) {
				Float pointersDst1 = (float) Math.hypot(event.getX(1) - event.getX(0), event.getY(1) - event
						.getY(0));
				float scale = (pointersDst1 / pointersDst0 - 1) * FONTSIZE_SCALE_FACTOR + 1;
				float fontsize1 = fontsize0 * scale;
				scroll = startScroll * scale;
				previewFontsize(fontsize1);
			}
		}
	}
	
	private void onTouchUp(MotionEvent event) {
	}
	
	private void onTouchPointerDown(MotionEvent event) {
		pointersDst0 = (float) Math.hypot(event.getX(1) - event.getX(0), event.getY(1) - event.getY(0));
		fontsize0 = fontsizeTmp;
		startScroll = scroll;
	}
	
	private void onTouchPointerUp(MotionEvent event) {
		pointersDst0 = null; // reset initial length
		startScroll = scroll;
		// leave a pointer which is still active
		Integer pointerIndex = 0;
		if (event.getPointerCount() >= 2) {
			for (int i = 0; i < event.getPointerCount(); i++) {
				if (i != event.getActionIndex()) {
					pointerIndex = i;
					break;
				}
			}
		}
		startTouchY = event.getY(pointerIndex);
		
		songPreviewController.get().onFontsizeChangedEvent(fontsizeTmp);
	}
	
	public void onClick() {
		if (quickMenu.get().isVisible()) {
			quickMenu.get().onScreenClicked();
		} else {
			if (autoscroll.get().isRunning()) {
				autoscroll.get().onAutoscrollStopUIEvent();
			} else {
				quickMenu.get().setVisible(true);
			}
		}
	}
	
	public void setCRDModel(LyricsModel lyricsModel) {
		this.lyricsModel = lyricsModel;
		this.lyricsRenderer = new LyricsRenderer(this, lyricsModel);
		repaint();
	}
	
	public void setFontSizes(float fontsizeDp) {
		this.fontsizeTmp = fontsizeDp;
	}
	
	private float getFontsizePx() {
		return windowManagerService.dp2px(this.fontsizeTmp);
	}
	
	private float getLineheightPx() {
		return getFontsizePx() * LINEHEIGHT_SCALE_FACTOR;
	}
	
	public float getScroll() {
		return scroll;
	}
	
	float getMaxScroll() {
		float bottomY = getTextBottomY();
		float reserve = EOF_SCROLL_RESERVE * h;
		if (bottomY > h) {
			return bottomY + reserve - h;
		} else {
			return 0; // no scrolling possibility
		}
	}
	
	private float getTextBottomY() {
		if (lyricsModel == null)
			return 0;
		List<LyricsLine> lines = lyricsModel.getLines();
		if (lines == null || lines.isEmpty())
			return 0;
		LyricsLine lastLine = lines.get(lines.size() - 1);
		if (lastLine == null)
			return 0;
		float lineheight = getLineheightPx();
		return lastLine.getY() * lineheight + lineheight;
	}
	
	public int getMaxContentHeight() {
		float bottomY = getTextBottomY();
		float reserve = EOF_SCROLL_RESERVE * h;
		if (bottomY > h) {
			return (int) (bottomY + reserve);
		} else {
			return h; // no scroll possibility
		}
	}
	
	private void previewFontsize(float fontsize1) {
		int minScreen = w > h ? h : w;
		if (fontsize1 >= 5 && fontsize1 <= minScreen / 5) {
			setFontSizes(fontsize1);
			repaint();
		}
	}
	
	/**
	 * @param lineheightPart lineheight part to move [em]
	 * @return
	 */
	public boolean scrollByLines(float lineheightPart) {
		return scrollByPx(lineheightPart * getLineheightPx());
	}
	
	public boolean scrollByPx(float px) {
		scroll += px;
		boolean scrollable = true;
		float maxScroll = getMaxScroll();
		// cut off
		if (scroll < 0) {
			scroll = 0;
			scrollable = false;
		}
		if (scroll > maxScroll) {
			scroll = maxScroll;
			scrollable = false;
		}
		repaint();
		return scrollable;
	}
	
	public boolean canScrollDown() {
		return scroll < getMaxScroll();
	}
	
	public void setQuickMenuView(View quickMenuView) {
		quickMenu.get().setQuickMenuView(quickMenuView);
		quickMenu.get().setVisible(false);
	}
	
	
	public void onManuallyScrolled(int dy) {
		// monitor scroll changes
		float dScroll = -dy;
		if (Math.abs(dScroll) > MIN_SCROLL_EVENT) {
			autoscroll.get().onCanvasScrollEvent(dScroll, scroll);
		}
	}
}
