package igrek.songbook.graphics.canvas.quickmenu;

import igrek.songbook.graphics.canvas.CanvasGraphics;
import igrek.songbook.graphics.canvas.enums.Align;
import igrek.songbook.graphics.canvas.enums.Font;

public class QuickMenuButton {

    private String text;

    private float rx;
    private float ry;
    private float rw;
    private float rh;

    private ButtonClickedAction action;

    private final int backgroundColor = 0x505050;
    private final int backgroundAlpha = 230;

    private final int outlineColor = 0x303030;
    private final float outlineThickness = 2.5f;

    private final int textColor = 0xffffff;

    public QuickMenuButton(String text, float rx, float ry, float rw, float rh, ButtonClickedAction action) {
        this.text = text;
        this.rx = rx;
        this.ry = ry;
        this.rw = rw;
        this.rh = rh;
        this.action = action;
    }

    public String getText() {
        return text;
    }

    public void draw(CanvasGraphics canvas) {

        float w = canvas.getW();
        float h = canvas.getH();

        //tło
        canvas.setColor(backgroundColor, backgroundAlpha);
        canvas.fillRectWH(rx * w, ry * h, rw * w, rh * h);

        //ramka
        canvas.setColor(outlineColor, backgroundAlpha);
        canvas.outlineRectWH(rx * w, ry * h, rw * w, rh * h, outlineThickness);

        //tekst
        canvas.setColor(textColor);
        canvas.setFont(Font.FONT_NORMAL);
        canvas.drawText(getText(), (rx + rw / 2) * w, (ry + rh / 2) * h, Align.CENTER);
    }

    public boolean click(float clickRx, float clickRy) {
        //mieści się w obszarze
        if (clickRx >= rx && clickRx <= rx + rw && clickRy >= ry && clickRy <= ry + rh) {
            if (action != null) {
                action.onClicked();
            }
            return true;
        }
        return false;
    }
}
