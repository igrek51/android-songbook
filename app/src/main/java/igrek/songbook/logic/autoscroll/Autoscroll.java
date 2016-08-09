package igrek.songbook.logic.autoscroll;

import android.os.Handler;

import igrek.songbook.graphics.gui.GUIListener;

public class Autoscroll {

    private AutoscrollState state;

    //TODO skalowanie czcionki zmienia skaluje intervał / step

    private long waitTime = 12000; // [ms]
    private long intervalTime = 140; // [ms]
    private float intervalStep = 1.0f; // [px]

    private long startTime; // [ms]

    private final float START_NO_WAITING_MIN_SCROLL = 5.0f;

    private Handler timerHandler;
    private Runnable timerRunnable;

    private GUIListener guiListener;

    public Autoscroll(GUIListener guiListener) {
        this.guiListener = guiListener;
        timerHandler = new Handler();
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (state == AutoscrollState.OFF) return;
                handleAutoscroll();
            }
        };
        reset();
    }

    public void reset() {
        stop();
    }

    public void start(float scroll) {
        if (scroll <= START_NO_WAITING_MIN_SCROLL) {
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
        return state.equals(AutoscrollState.WAITING) || state.equals(AutoscrollState.SCROLLING);
    }

    public boolean isWaiting() {
        return state.equals(AutoscrollState.WAITING);
    }

    public boolean isScrolling() {
        return state.equals(AutoscrollState.SCROLLING);
    }

    private void handleAutoscroll() {
        if (state == AutoscrollState.WAITING) {
            long remainingTimeMs = waitTime + startTime - System.currentTimeMillis();
            if (remainingTimeMs <= 0) {
                state = AutoscrollState.SCROLLING;
                timerHandler.postDelayed(timerRunnable, 0);
                guiListener.onAutoscrollStarted();
            } else {
                timerHandler.postDelayed(timerRunnable, remainingTimeMs % 1001);
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
}
