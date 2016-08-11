package igrek.songbook.logic.autoscroll;

import android.os.Handler;

import igrek.songbook.graphics.gui.GUIListener;
import igrek.songbook.output.Output;

public class Autoscroll {

    private AutoscrollState state;

    private long waitTime = 30000; // [ms]
    private long intervalTime = 250; // [ms]
    private float intervalStep = 2.0f; // [px]

    private float fontsize;

    private long startTime; // [ms]

    private final long MIN_INTERVAL_TIME = 5;
    private final float START_NO_WAITING_MIN_SCROLL_FACTOR = 1.0f;

    private final float AUTOCHANGE_SPEED_SCALE = 0.0027f;

    private Handler timerHandler;
    private Runnable timerRunnable;

    private GUIListener guiListener;

    public Autoscroll(GUIListener guiListener, float fontsize) {
        this.guiListener = guiListener;
        timerHandler = new Handler();
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (state == AutoscrollState.OFF) return;
                handleAutoscroll();
            }
        };
        this.fontsize = fontsize;
        reset();
    }

    public void reset() {
        stop();
    }

    public void start(float scroll) {
        if (scroll <= START_NO_WAITING_MIN_SCROLL_FACTOR * fontsize) {
            start(true);
        } else {
            start(false);
        }
    }

    public void start(boolean withWaiting) {
        if (isRunning()) {
            stop();
        }
        if (guiListener.canAutoScroll()) {
            if (withWaiting) {
                state = AutoscrollState.WAITING;
            } else {
                state = AutoscrollState.SCROLLING;
            }
            startTime = System.currentTimeMillis();
            timerHandler.postDelayed(timerRunnable, 0);
        }
    }

    public void stop() {
        state = AutoscrollState.OFF;
        timerHandler.removeCallbacks(timerRunnable);
    }

    public boolean isRunning() {
        return state == AutoscrollState.WAITING || state == AutoscrollState.SCROLLING;
    }

    public boolean isWaiting() {
        return state == AutoscrollState.WAITING;
    }

    public boolean isScrolling() {
        return state == AutoscrollState.SCROLLING;
    }

    private void handleAutoscroll() {
        if (state == AutoscrollState.WAITING) {
            long remainingTimeMs = waitTime + startTime - System.currentTimeMillis();
            if (remainingTimeMs <= 0) {
                state = AutoscrollState.SCROLLING;
                timerHandler.postDelayed(timerRunnable, 0);
                guiListener.onAutoscrollStarted();
            } else {
                long delay = remainingTimeMs > 1000 ? 1000 : remainingTimeMs; //nasycenie do 1000
                timerHandler.postDelayed(timerRunnable, delay);
                guiListener.autoscrollRemainingWaitTime(remainingTimeMs);
            }
        } else if (state == AutoscrollState.SCROLLING) {
            if (guiListener.auscrollScrollBy(intervalStep)) {
                timerHandler.postDelayed(timerRunnable, intervalTime);
            } else {
                stop();
                guiListener.onAutoscrollEnded();
            }
        }
    }

    public void setFontsize(float fontsize) {
        //TODO skalowanie czcionki zmienia skaluje intervał / step
        this.fontsize = fontsize;
    }

    public void handleCanvasScroll(float dScroll) {

        if (state == AutoscrollState.WAITING) {
            if (dScroll > 0) {
                state = AutoscrollState.SCROLLING;
                guiListener.onAutoscrollStarted();
            }
        } else if (state == AutoscrollState.SCROLLING) {
            if (dScroll > 0) { //przyspieszanie przewijania
                intervalTime -= intervalTime * dScroll * AUTOCHANGE_SPEED_SCALE;
            } else if (dScroll < 0) { //zwalnianie przewijania
                intervalTime -= intervalTime * dScroll * AUTOCHANGE_SPEED_SCALE;
            }
            if (intervalTime < MIN_INTERVAL_TIME) {
                intervalTime = MIN_INTERVAL_TIME;
            }
            Output.info("Nowy interwał autoprzewijania: " + intervalTime + " ms");
        }
    }
}
