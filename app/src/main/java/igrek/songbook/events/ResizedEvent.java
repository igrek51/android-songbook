package igrek.songbook.events;

import igrek.songbook.logic.controller.dispatcher.IEvent;

public class ResizedEvent implements IEvent {

    private int w;
    private int h;

    public ResizedEvent(int w, int h) {
        this.w = w;
        this.h = h;
    }

    public int getW() {
        return w;
    }

    public int getH() {
        return h;
    }
}
