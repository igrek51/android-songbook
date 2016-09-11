package igrek.songbook.logic.events;

import igrek.songbook.logic.controller.dispatcher.IEvent;

public class TransposedEvent implements IEvent {

    private int t;

    public TransposedEvent(int t) {
        this.t = t;
    }

    public int getT() {
        return t;
    }
}
