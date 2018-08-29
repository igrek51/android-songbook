package igrek.songbook.view.songpreview;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;

import java.util.List;

import javax.inject.Inject;

import dagger.Lazy;
import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.domain.crd.CRDLine;
import igrek.songbook.domain.crd.CRDModel;
import igrek.songbook.service.autoscroll.AutoscrollService;
import igrek.songbook.service.layout.songpreview.SongPreviewController;
import igrek.songbook.view.songpreview.base.BaseCanvasGraphics;
import igrek.songbook.view.songpreview.quickmenu.QuickMenu;

public class CanvasGraphics extends BaseCanvasGraphics {
	
	private final float EOF_SCROLL_RESERVE = 0.09f;
	private final float LINEHEIGHT_SCALE_FACTOR = 1.02f;
	private final float FONTSIZE_SCALE_FACTOR = 0.6f;
	private final float GESTURE_TRANSPOSE_MIN_DX = 0.4f;
	private final float GESTURE_AUTOSCROLL_BOTTOM_REGION = 0.6f;
	private final float GESTURE_CLICK_MAX_HYPOT = 8.0f;
	private final long GESTURE_CLICK_MAX_TIME = 500;
	private final float MIN_SCROLL_EVENT = 15f;
	@Inject
	Lazy<AutoscrollService> autoscroll;
	@Inject
	Lazy<SongPreviewController> songPreviewController;
	@Inject
	Lazy<QuickMenu> quickMenu;
	private CRDModel crdModel = null;
	private float scroll = 0;
	private float startScroll = 0;
	private float fontsize;
	private float lineheight;
	private Float pointersDst0 = null;
	private Float fontsize0 = null;
	private LyricsRenderer lyricsRenderer;
	
	public CanvasGraphics(Context context) {
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
		crdModel = null;
	}
	
	public void setCRDModel(CRDModel crdModel) {
		this.crdModel = crdModel;
		this.lyricsRenderer = new LyricsRenderer(this, crdModel);
		repaint();
	}
	
	public void setFontSizes(float fontsize) {
		this.fontsize = fontsize;
		this.lineheight = fontsize * LINEHEIGHT_SCALE_FACTOR;
	}
	
	public float getScroll() {
		return scroll;
	}
	
	@Override
	public void init() {
		songPreviewController.get().onGraphicsInitializedEvent(w, h, paint);
	}
	
	@Override
	public void onRepaint() {
		drawBackground();
		if (this.lyricsRenderer != null) {
			lyricsRenderer.drawScrollBar();
			lyricsRenderer.drawFileContent(fontsize, lineheight);
		}
		quickMenu.get().draw();
	}
	
	private void drawBackground() {
		setColor(0x000000);
		clearScreen();
	}
	
	@Override
	protected void onTouchDown(MotionEvent event) {
		super.onTouchDown(event);
		startScroll = scroll;
		pointersDst0 = null;
	}
	
	@Override
	protected void onTouchMove(MotionEvent event) {
		if (event.getPointerCount() >= 2) {
			if (pointersDst0 != null) {
				Float pointersDst1 = (float) Math.hypot(event.getX(1) - event.getX(0), event.getY(1) - event
						.getY(0));
				float scale = (pointersDst1 / pointersDst0 - 1) * FONTSIZE_SCALE_FACTOR + 1;
				float fontsize1 = fontsize0 * scale;
				previewFontsize(fontsize1);
			}
		} else {
			scroll = startScroll + startTouchY - event.getY();
			float maxScroll = getMaxScroll();
			if (scroll < 0)
				scroll = 0; // too much scrolling up
			if (scroll > maxScroll)
				scroll = maxScroll; // too much scrolling down
			repaint();
		}
	}
	
	float getMaxScroll() {
		float bottomY = getTextBottomY();
		float reserve = EOF_SCROLL_RESERVE * h;
		if (bottomY > h) {
			return bottomY + reserve - h;
		} else {
			// no scroll possibility
			return 0;
		}
	}
	
	@Override
	protected void onTouchUp(MotionEvent event) {
		float deltaX = event.getX() - startTouchX;
		float deltaY = event.getY() - startTouchY;
		// monitor scroll changes
		float dScroll = -deltaY;
		if (Math.abs(dScroll) > MIN_SCROLL_EVENT) {
			autoscroll.get().onCanvasScrollEvent(dScroll, scroll);
		}
		
		// quick tap on bottom - turn on autoscroll
		float hypot = (float) Math.hypot(deltaX, deltaY);
		if (hypot <= GESTURE_CLICK_MAX_HYPOT) { // tap in one area
			if (System.currentTimeMillis() - startTouchTime <= GESTURE_CLICK_MAX_TIME) { //quick tap
				if (onScreenClicked(event.getX(), event.getY())) {
					repaint();
				}
			}
		}
	}
	
	private boolean onScreenClicked(float x, float y) {
		if (quickMenu.get().isVisible()) {
			return quickMenu.get().onScreenClicked(x, y);
		} else {
			if (autoscroll.get().isRunning()) {
				autoscroll.get().onAutoscrollStopUIEvent();
			} else {
				if (y >= h * GESTURE_AUTOSCROLL_BOTTOM_REGION) {  //tap on a screen bottom
					autoscroll.get().onAutoscrollStartUIEvent();
				} else {
					quickMenu.get().setVisible(true);
				}
			}
			return true;
		}
	}
	
	@Override
	protected void onTouchPointerUp(MotionEvent event) {
		songPreviewController.get().onFontsizeChangedEvent(fontsize);
		
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
	}
	
	@Override
	protected void onTouchPointerDown(MotionEvent event) {
		pointersDst0 = (float) Math.hypot(event.getX(1) - event.getX(0), event.getY(1) - event.getY(0));
		fontsize0 = fontsize;
	}
	
	private float getTextBottomY() {
		if (crdModel == null)
			return 0;
		List<CRDLine> lines = crdModel.getLines();
		if (lines == null || lines.isEmpty())
			return 0;
		CRDLine lastLine = lines.get(lines.size() - 1);
		if (lastLine == null)
			return 0;
		return lastLine.getY() * lineheight + lineheight;
	}
	
	private void previewFontsize(float fontsize1) {
		int minScreen = w > h ? h : w;
		if (fontsize1 >= 5 && fontsize1 <= minScreen / 5) {
			setFontSizes(fontsize1);
			repaint();
		}
	}
	
	public boolean autoscrollBy(float intervalStep) {
		boolean scrollable = true;
		scroll += intervalStep;
		float maxScroll = getMaxScroll();
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
	
	public boolean canAutoScroll() {
		return scroll < getMaxScroll();
	}
	
	public void setQuickMenuView(View quickMenuView) {
		quickMenu.get().setQuickMenuView(quickMenuView);
		quickMenu.get().setVisible(false);
	}
}
