package igrek.songbook.events;

import igrek.songbook.logic.controller.dispatcher.IEvent;

public class ShowQuickMenuEvent implements IEvent {

    private boolean show;

    public ShowQuickMenuEvent(boolean show) {
        this.show = show;
    }

    public boolean isShow() {
        return show;
    }
}
