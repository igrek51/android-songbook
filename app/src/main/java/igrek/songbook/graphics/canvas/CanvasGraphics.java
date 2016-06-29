package igrek.songbook.graphics.canvas;

import android.content.Context;

import igrek.songbook.graphics.gui.GUIListener;
import igrek.songbook.logic.crd.CRDFragment;
import igrek.songbook.logic.crd.CRDLine;
import igrek.songbook.logic.crd.CRDModel;
import igrek.songbook.logic.crd.CRDTextType;
import igrek.songbook.settings.Config;

public class CanvasGraphics extends BaseCanvasGraphics {

    public String filename = null;

    private CRDModel crdModel = null;

    private float scroll = 0;
    private float startScroll = 0;

    public CanvasGraphics(Context context, GUIListener guiListener) {
        super(context, guiListener);
    }

    public void setCRDModel(CRDModel crdModel) {
        this.crdModel = crdModel;
        repaint();
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    @Override
    public void init() {
        setFontSize(Config.Fonts.fontsize);
        guiListener.onGraphicsInitialized(w, h, paint);
    }

    @Override
    public void onRepaint() {

        drawBackground();

        drawFileContent();
    }

    private void drawFileContent() {
        setFontSize(Config.Fonts.fontsize);

        setColor(0xffffff);

        if (crdModel != null) {
            for (CRDLine line : crdModel.getLines()) {
                drawLine(line, scroll, Config.Fonts.lineheight);
            }
        }
    }

    private void drawBackground() {
        setColor(Config.Colors.background);
        clearScreen();
    }

    private void drawLine(CRDLine line, float scroll, float lineheight) {
        float y = line.getY() - scroll;
        if (y > h) return;
        if (y + lineheight < 0) return;
        final float yOffset = lineheight;

        for (CRDFragment fragment : line.getFragments()) {

            if (fragment.getType() == CRDTextType.REGULAR_TEXT) {
                setColor(0xffffff);
            } else if (fragment.getType() == CRDTextType.CHORDS) {
                setColor(0xf00000);
            }

            drawTextUnaligned(fragment.getText(), fragment.getX(), y + yOffset);
        }
    }

    @Override
    protected void onTouchDown(float touchX, float touchY) {
        super.onTouchDown(touchX, touchY);
        startScroll = scroll;
    }

    @Override
    protected void onTouchMove(float touchX, float touchY) {
        scroll = startScroll + startTouchY - touchY;
        if (scroll < 0) scroll = 0;
        repaint();
    }
}
