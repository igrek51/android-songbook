package igrek.songbook.events;

import igrek.songbook.logic.controller.dispatcher.IEvent;

public class CanvasScrollEvent implements IEvent {

    private float dScroll;
    private float scroll;

    public CanvasScrollEvent(float dScroll, float scroll) {
        this.dScroll = dScroll;
        this.scroll = scroll;
    }

    public float getdScroll() {
        return dScroll;
    }

    public float getScroll() {
        return scroll;
    }

}
