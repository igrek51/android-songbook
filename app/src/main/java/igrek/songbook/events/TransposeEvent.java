package igrek.songbook.events;

import igrek.songbook.logic.controller.dispatcher.IEvent;

public class TransposeEvent implements IEvent {

    private int t;

    public TransposeEvent(int t) {
        this.t = t;
    }

    public int getT() {
        return t;
    }
}
