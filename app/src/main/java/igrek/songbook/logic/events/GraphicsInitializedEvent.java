package igrek.songbook.logic.events;

import android.graphics.Paint;

import igrek.songbook.logic.controller.dispatcher.IEvent;

public class GraphicsInitializedEvent implements IEvent {

    private int w;
    private int h;
    private Paint paint;

    public GraphicsInitializedEvent(int w, int h, Paint paint) {
        this.w = w;
        this.h = h;
        this.paint = paint;
    }

    public int getW() {
        return w;
    }

    public int getH() {
        return h;
    }

    public Paint getPaint() {
        return paint;
    }
}
