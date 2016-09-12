package igrek.songbook.graphics.canvas;

import android.content.Context;
import android.view.MotionEvent;

import java.util.List;

import igrek.songbook.events.AutoscrollStartRequestUIEvent;
import igrek.songbook.events.AutoscrollToggleRequestEvent;
import igrek.songbook.events.CanvasClickedEvent;
import igrek.songbook.events.CanvasScrollEvent;
import igrek.songbook.events.FontsizeChangedEvent;
import igrek.songbook.events.GraphicsInitializedEvent;
import igrek.songbook.events.ShowQuickMenuEvent;
import igrek.songbook.events.TransposedEvent;
import igrek.songbook.graphics.Colors;
import igrek.songbook.graphics.canvas.enums.Align;
import igrek.songbook.graphics.canvas.enums.Font;
import igrek.songbook.logic.autoscroll.Autoscroll;
import igrek.songbook.logic.controller.AppController;
import igrek.songbook.logic.controller.dispatcher.IEvent;
import igrek.songbook.logic.controller.dispatcher.IEventObserver;
import igrek.songbook.logic.controller.services.IService;
import igrek.songbook.logic.crdfile.CRDFragment;
import igrek.songbook.logic.crdfile.CRDLine;
import igrek.songbook.logic.crdfile.CRDModel;
import igrek.songbook.logic.crdfile.CRDTextType;
import igrek.songbook.logic.crdfile.ChordsManager;

//TODO jedna instacja, ponowne wykorzystanie klasy

public class CanvasGraphics extends BaseCanvasGraphics implements IService, IEventObserver {

    private CRDModel crdModel = null;

    private float scroll = 0;
    private float startScroll = 0;

    private float fontsize;
    private float lineheight;

    private final float EOF_SCROLL_RESERVE = 0.09f;
    private final float LINEHEIGHT_SCALE_FACTOR = 1.02f;
    private final float FONTSIZE_SCALE_FACTOR = 0.6f;

    private final float GESTURE_TRANSPOSE_MIN_DX = 0.4f;
    private final float GESTURE_AUTOSCROLL_BOTTOM_REGION = 0.6f;
    private final float GESTURE_CLICK_MAX_HYPOT = 7.0f;
    private final long GESTURE_CLICK_MAX_TIME = 500;

    private final float MIN_SCROLL_EVENT = 15f;

    private final float MENU_TRANSPOSE_BUTTON_H = 0.2f;
    private final float MENU_AUTOSCROLL_BUTTON_H = 0.2f;

    private Float pointersDst0 = null;
    private Float fontsize0 = null;

    private boolean menuVisible = false;

    public CanvasGraphics(Context context) {
        super(context);
        AppController.registerService(this);

        AppController.clearEventObservers(ShowQuickMenuEvent.class);
        AppController.registerEventObserver(ShowQuickMenuEvent.class, this);
    }

    public void setCRDModel(CRDModel crdModel) {
        this.crdModel = crdModel;
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
        setFontSize(fontsize);
        setFont(Font.FONT_NORMAL);
        AppController.sendEvent(new GraphicsInitializedEvent(w, h, paint));
    }

    @Override
    public void onRepaint() {

        drawBackground();

        drawFileContent();

        drawMenu();
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

    private void drawMenu() {
        if (menuVisible) {

            setFont(Font.FONT_NORMAL);
            setColor(0x000000, 150);
            fillRect(0, 0, w, h);

            float radius = 0.01f * w;

            //TODO zamknąć w funkcję, buttony, poprawić wygląd

            //autocsroll
            setColor(0x505050, 230);
            fillRoundRect(0, h - MENU_AUTOSCROLL_BUTTON_H * h, w, h, radius);
            setColor(0x303030, 230);
            outlineRectWH(0, h - MENU_AUTOSCROLL_BUTTON_H * h, w, h, 2.0f);
            setColor(0xffffff);
            drawText("Autoscroll", w / 2, h - MENU_AUTOSCROLL_BUTTON_H * h * 0.5f, Align.CENTER);

            //transpozycja
            float y = h - MENU_AUTOSCROLL_BUTTON_H * h - MENU_TRANSPOSE_BUTTON_H * h;

            setColor(0x505050, 230);
            fillRoundRectWH(0, y, w / 5, MENU_TRANSPOSE_BUTTON_H * h, radius);
            setColor(0x303030, 230);
            outlineRectWH(0, y, w / 5, MENU_TRANSPOSE_BUTTON_H * h, 2.0f);

            setColor(0x505050, 230);
            fillRoundRectWH(w / 5, y, w / 5, MENU_TRANSPOSE_BUTTON_H * h, radius);
            setColor(0x303030, 230);
            outlineRectWH(w / 5, y, w / 5, MENU_TRANSPOSE_BUTTON_H * h, 2.0f);

            setColor(0x505050, 230);
            fillRoundRectWH(2 * w / 5, y, w / 5, MENU_TRANSPOSE_BUTTON_H * h, radius);
            setColor(0x303030, 230);
            outlineRectWH(2 * w / 5, y, w / 5, MENU_TRANSPOSE_BUTTON_H * h, 2.0f);

            setColor(0x505050, 230);
            fillRoundRectWH(3 * w / 5, y, w / 5, MENU_TRANSPOSE_BUTTON_H * h, radius);
            setColor(0x303030, 230);
            outlineRectWH(3 * w / 5, y, w / 5, MENU_TRANSPOSE_BUTTON_H * h, 2.0f);

            setColor(0x505050, 230);
            fillRoundRectWH(4 * w / 5, y, w / 5, MENU_TRANSPOSE_BUTTON_H * h, radius);
            setColor(0x303030, 230);
            outlineRectWH(4 * w / 5, y, w / 5, MENU_TRANSPOSE_BUTTON_H * h, 2.0f);

            y = h - MENU_AUTOSCROLL_BUTTON_H * h - MENU_TRANSPOSE_BUTTON_H * h * 0.5f;
            setColor(0xffffff);
            drawText("-5", w / 10, y, Align.CENTER);
            drawText("-1", w / 10 + w / 5, y, Align.CENTER);
            drawText("0", w / 10 + 2 * w / 5, y, Align.CENTER);
            drawText("+1", w / 10 + 3 * w / 5, y, Align.CENTER);
            drawText("+5", w / 10 + 4 * w / 5, y, Align.CENTER);
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
            float maxScroll = getMaxScroll();
            if (scroll < 0) scroll = 0; //za duże przeskrolowanie w górę
            if (scroll > maxScroll) scroll = maxScroll; // za duże przescrollowanie w dół
            repaint();
        }
    }

    private float getMaxScroll() {
        float bottomY = getTextBottomY();
        float reserve = EOF_SCROLL_RESERVE * h;
        if (bottomY > h) {
            return bottomY + reserve - h;
        } else {
            //brak możliwości scrollowania
            return 0;
        }
    }

    @Override
    protected void onTouchUp(MotionEvent event) {

        float deltaX = event.getX() - startTouchX;
        float deltaY = event.getY() - startTouchY;
        // monitorowanie zmiany przewijania
        float dScroll = -deltaY;
        if (Math.abs(dScroll) > MIN_SCROLL_EVENT) {
            AppController.sendEvent(new CanvasScrollEvent(dScroll, scroll));
        }

        //  GESTY
        //gest smyrania w lewo i prawo
        if (Math.abs(deltaX) > Math.abs(deltaY)) {
            if (Math.abs(deltaX) >= GESTURE_TRANSPOSE_MIN_DX * w) {
                if (deltaX < 0) {
                    AppController.sendEvent(new TransposedEvent(-1));
                    return;
                } else if (deltaX > 0) {
                    AppController.sendEvent(new TransposedEvent(+1));
                    return;
                }
            }
        }
        //włączenie autoscrolla - szybkie kliknięcie na dole
        float hypot = (float) Math.hypot(deltaX, deltaY);
        if (hypot <= GESTURE_CLICK_MAX_HYPOT) { //kliknięcie w jednym miejscu
            if (System.currentTimeMillis() - startTouchTime <= GESTURE_CLICK_MAX_TIME) { //szybkie kliknięcie
                if (clickedOnCanvas(event.getX(), event.getY())) {
                    repaint();
                }
            }
        }
    }

    private boolean clickedOnCanvas(float x, float y) {

        //TODO uprościć komunikaty
        //TODO zgeneralizowane wiadomości o rozpoczęciu / zakończeniu autoscrolla

        if (menuVisible) {

            if (y >= h - h * MENU_AUTOSCROLL_BUTTON_H) {
                //Autoscroll

                AppController.sendEvent(new AutoscrollToggleRequestEvent());

                menuVisible = false;
                return true;

            } else if (y >= h - h * MENU_AUTOSCROLL_BUTTON_H - h * MENU_TRANSPOSE_BUTTON_H) {
                //transpozycja

                Integer transpose;
                if (x < w / 5) {
                    transpose = -5;
                } else if (x < 2 * w / 5) {
                    transpose = -1;
                } else if (x < 3 * w / 5) {
                    transpose = null;
                } else if (x < 4 * w / 5) {
                    transpose = +1;
                } else {
                    transpose = +5;
                }
                if (transpose != null) {
                    AppController.sendEvent(new TransposedEvent(transpose.intValue()));
                } else {
                    ChordsManager chordsManager = AppController.getService(ChordsManager.class);
                    AppController.sendEvent(new TransposedEvent(-chordsManager.getTransposed()));
                }

                return true;
            }

            menuVisible = false;
            return true;

        } else {

            Autoscroll autoscroll = AppController.getService(Autoscroll.class);
            if (autoscroll.isRunning()) {
                AppController.sendEvent(new CanvasClickedEvent());
                return true;
            } else {

                if (y >= h * GESTURE_AUTOSCROLL_BOTTOM_REGION) {  //na dole

                    AppController.sendEvent(new AutoscrollStartRequestUIEvent());
                    return true;

                } else {

                    menuVisible = true;
                    return true;

                }
            }
        }
    }

    @Override
    protected void onTouchPointerUp(MotionEvent event) {
        AppController.sendEvent(new FontsizeChangedEvent(fontsize));
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

    public boolean isMenuVisible() {
        return menuVisible;
    }

    @Override
    public void onEvent(IEvent event) {
        if (event instanceof ShowQuickMenuEvent) {
            menuVisible = ((ShowQuickMenuEvent) event).isShow();
        }
    }
}
