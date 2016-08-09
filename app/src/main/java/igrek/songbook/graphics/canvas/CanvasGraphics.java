package igrek.songbook.graphics.canvas;

import android.content.Context;
import android.view.MotionEvent;

import java.util.List;

import igrek.songbook.graphics.Colors;
import igrek.songbook.graphics.canvas.enums.Font;
import igrek.songbook.graphics.gui.GUIListener;
import igrek.songbook.logic.crdfile.CRDFragment;
import igrek.songbook.logic.crdfile.CRDLine;
import igrek.songbook.logic.crdfile.CRDModel;
import igrek.songbook.logic.crdfile.CRDTextType;

public class CanvasGraphics extends BaseCanvasGraphics {

    //TODO komunikaty wyświetlane użytkownikowi (echo)
    //TODO test ze zmianą orientacji ekranu

    private CRDModel crdModel = null;

    private float scroll = 0;
    private float startScroll = 0;

    private float fontsize;
    private float lineheight;

    private final float EOF_SCROLL_RESERVE = 0.1f;
    private final float LINEHEIGHT_SCALE_FACTOR = 1.02f;
    private final float FONTSIZE_SCALE_FACTOR = 0.7f;

    private Float pointersDst0 = null;
    private Float fontsize0 = null;

    public CanvasGraphics(Context context, GUIListener guiListener) {
        super(context, guiListener);
    }

    public void setCRDModel(CRDModel crdModel) {
        this.crdModel = crdModel;
        repaint();
    }

    public void setFontSizes(float fontsize) {
        this.fontsize = fontsize;
        this.lineheight = fontsize * LINEHEIGHT_SCALE_FACTOR;
    }

    @Override
    public void init() {
        setFontSize(fontsize);
        setFont(Font.FONT_NORMAL);
        guiListener.onGraphicsInitialized(w, h, paint);
    }

    @Override
    public void onRepaint() {

        drawBackground();

        drawFileContent();
    }

    private void drawFileContent() {
        setFontSize(fontsize);

        setColor(0xffffff);

        if (crdModel != null) {
            for (CRDLine line : crdModel.getLines()) {
                drawTextLine(line, scroll);
            }
        }
    }

    private void drawBackground() {
        setColor(Colors.background);
        clearScreen();
    }

    private void drawTextLine(CRDLine line, float scroll) {
        float y = line.getY() * lineheight - scroll;
        if (y > h) return;
        if (y + lineheight < 0) return;

        for (CRDFragment fragment : line.getFragments()) {

            if (fragment.getType() == CRDTextType.REGULAR_TEXT) {
                setFont(Font.FONT_NORMAL);
                setColor(0xffffff);
            } else if (fragment.getType() == CRDTextType.CHORDS) {
                setFont(Font.FONT_BOLD);
                setColor(0xf00000);
            }

            drawTextUnaligned(fragment.getText(), fragment.getX() * fontsize, y + lineheight);
        }
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
                Float pointersDst1 = (float) Math.hypot(event.getX(1) - event.getX(0), event.getY(1) - event.getY(0));
                float scale = (pointersDst1 / pointersDst0 - 1) * FONTSIZE_SCALE_FACTOR + 1;
                float fontsize1 = fontsize0 * scale;
                previewFontsize(fontsize1);
            }
        } else {
            scroll = startScroll + startTouchY - event.getY();
            if (scroll < 0) scroll = 0; //za duże przeskrolowanie w górę
            float bottomY = getTextBottomY();
            float reserve = EOF_SCROLL_RESERVE * h;
            if (bottomY > h) {
                if (bottomY + reserve - scroll < h) { //za duże przescrollowanie w dół
                    scroll = bottomY + reserve - h;
                }
            } else {
                //brak możliwości scrollowania
                scroll = 0;
            }
            repaint();
        }
    }

    @Override
    protected void onTouchUp(MotionEvent event) {
        float deltaX = event.getX() - startTouchX;
        float deltaY = event.getY() - startTouchY;
        if (Math.abs(deltaX) > Math.abs(deltaY)) {
            if (Math.abs(deltaX) >= 0.4 * w) {
                if (deltaX < 0) {
                    guiListener.onTransposed(-1);
                } else if (deltaX > 0) {
                    guiListener.onTransposed(+1);
                }
            }
        }
    }

    @Override
    protected void onTouchPointerUp(MotionEvent event) {
        guiListener.onFontsizeChanged(fontsize);
        pointersDst0 = null; //reset poczatkowej długości
        startTouchY = event.getY(0); // brak przewijania
        startScroll = scroll;
    }

    @Override
    protected void onTouchPointerDown(MotionEvent event) {
        pointersDst0 = (float) Math.hypot(event.getX(1) - event.getX(0), event.getY(1) - event.getY(0));
        fontsize0 = fontsize;
    }

    private float getTextBottomY() {
        if (crdModel == null) return 0;
        List<CRDLine> lines = crdModel.getLines();
        if (lines == null || lines.isEmpty()) return 0;
        CRDLine lastLine = lines.get(lines.size() - 1);
        if (lastLine == null) return 0;
        return lastLine.getY() * lineheight + lineheight;
    }

    private void previewFontsize(float fontsize1) {
        int minScreen = w > h ? h : w;
        if (fontsize1 >= 5 && fontsize1 <= minScreen / 5) {
            setFontSizes(fontsize1);
            repaint();
        }
    }
}
