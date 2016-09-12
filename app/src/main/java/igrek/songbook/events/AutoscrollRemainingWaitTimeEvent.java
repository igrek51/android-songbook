package igrek.songbook.events;

import igrek.songbook.logic.controller.dispatcher.IEvent;

public class AutoscrollRemainingWaitTimeEvent implements IEvent {

    private long ms;

    public AutoscrollRemainingWaitTimeEvent(long ms) {
        this.ms = ms;
    }

    public long getMs() {
        return ms;
    }
}
