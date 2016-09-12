package igrek.songbook.events;

import igrek.songbook.logic.controller.dispatcher.IEvent;

public class FontsizeChangedEvent implements IEvent {

    private float fontsize;

    public FontsizeChangedEvent(float fontsize) {
        this.fontsize = fontsize;
    }

    public float getFontsize() {
        return fontsize;
    }
}
