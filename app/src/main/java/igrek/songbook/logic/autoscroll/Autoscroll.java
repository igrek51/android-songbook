package igrek.songbook.logic.autoscroll;

import android.os.Handler;

import igrek.songbook.graphics.canvas.CanvasGraphics;
import igrek.songbook.logger.Logs;
import igrek.songbook.logic.controller.AppController;
import igrek.songbook.logic.controller.dispatcher.IEvent;
import igrek.songbook.logic.controller.dispatcher.IEventObserver;
import igrek.songbook.logic.controller.services.IService;
import igrek.songbook.logic.crdfile.ChordsManager;
import igrek.songbook.logic.events.AutoscrollEndedEvent;
import igrek.songbook.logic.events.AutoscrollRemainingWaitTimeEvent;
import igrek.songbook.logic.events.AutoscrollStartRequestEvent;
import igrek.songbook.logic.events.AutoscrollStartedEvent;
import igrek.songbook.logic.events.AutoscrollStopRequestEvent;

public class Autoscroll implements IService, IEventObserver {

    private AutoscrollState state;

    private long waitTime = 32000; // [ms]
    private float intervalTime = 300; // [ms]
    private float intervalStep = 2.0f; // [px]

    private float fontsize;

    private long startTime; // [ms]

    private final long MIN_INTERVAL_TIME = 5;
    private final float START_NO_WAITING_MIN_SCROLL_FACTOR = 1.0f;

    private final float AUTOCHANGE_INTERVAL_SCALE = 0.0022f;
    private final float AUTOCHANGE_WAITING_SCALE = 7.0f;

    private Handler timerHandler;
    private Runnable timerRunnable;

    public Autoscroll() {

        timerHandler = new Handler();
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (state == AutoscrollState.OFF) return;
                handleAutoscroll();
            }
        };

        ChordsManager chordsManager = AppController.getService(ChordsManager.class);
        fontsize = chordsManager.getFontsize();

        AppController.registerEventObserver(AutoscrollStartRequestEvent.class, this);
        AppController.registerEventObserver(AutoscrollStopRequestEvent.class, this);

        reset();
    }

    public void reset() {
        stop();
    }

    public void start() {
        CanvasGraphics canvas = AppController.getService(CanvasGraphics.class);
        float scroll = canvas.getScroll();
        if (scroll <= START_NO_WAITING_MIN_SCROLL_FACTOR * fontsize) {
            start(true);
        } else {
            start(false);
        }
    }

    private void start(boolean withWaiting) {
        if (isRunning()) {
            stop();
        }
        CanvasGraphics canvas = AppController.getService(CanvasGraphics.class);
        if (canvas.canAutoScroll()) {
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

    private void handleAutoscroll() {
        if (state == AutoscrollState.WAITING) {
            long remainingTimeMs = waitTime + startTime - System.currentTimeMillis();
            if (remainingTimeMs <= 0) {
                state = AutoscrollState.SCROLLING;
                timerHandler.postDelayed(timerRunnable, 0);
                AppController.sendEvent(new AutoscrollStartedEvent());
            } else {
                long delay = remainingTimeMs > 1000 ? 1000 : remainingTimeMs; //nasycenie do 1000
                timerHandler.postDelayed(timerRunnable, delay);
                AppController.sendEvent(new AutoscrollRemainingWaitTimeEvent(remainingTimeMs));
            }
        } else if (state == AutoscrollState.SCROLLING) {
            CanvasGraphics canvas = AppController.getService(CanvasGraphics.class);
            if (canvas.autoscrollBy(intervalStep)) {
                timerHandler.postDelayed(timerRunnable, (long) intervalTime);
            } else {
                stop();
                AppController.sendEvent(new AutoscrollEndedEvent());
            }
        }
    }

    public void setFontsize(float fontsize) {
        //skalowanie czcionki zmienia skaluje intervał / step
        intervalTime = intervalTime * this.fontsize / fontsize;
        if (intervalTime < MIN_INTERVAL_TIME) {
            intervalTime = MIN_INTERVAL_TIME;
        }
        this.fontsize = fontsize;
        Logs.info("Nowy interwał autoprzewijania (po zmianie czcionki): " + intervalTime + " ms");
    }

    public void handleCanvasScroll(float dScroll, float scroll) {
        if (state == AutoscrollState.WAITING) {
            if (dScroll > 0) { //przyspieszanie przewijania
                state = AutoscrollState.SCROLLING;
                AppController.sendEvent(new AutoscrollStartedEvent());
            } else if (dScroll < 0) { //zwalnianie odliczania
                startTime -= (long) (dScroll * AUTOCHANGE_WAITING_SCALE);
                long remainingTimeMs = waitTime + startTime - System.currentTimeMillis();
                AppController.sendEvent(new AutoscrollRemainingWaitTimeEvent(remainingTimeMs));
            }
        } else if (state == AutoscrollState.SCROLLING) {
            if (dScroll > 0) { //przyspieszanie przewijania
                intervalTime -= intervalTime * dScroll * AUTOCHANGE_INTERVAL_SCALE;
            } else if (dScroll < 0) { //zwalnianie przewijania
                //przewinięcie na początek pliku
                if (scroll <= 0) {
                    //przejście w tryb waiting z dodaniem czasu
                    state = AutoscrollState.WAITING;
                    startTime = System.currentTimeMillis() - waitTime - (long) (dScroll * AUTOCHANGE_WAITING_SCALE);
                    long remainingTimeMs = waitTime + startTime - System.currentTimeMillis();
                    AppController.sendEvent(new AutoscrollRemainingWaitTimeEvent(remainingTimeMs));
                    return;
                } else {
                    intervalTime -= intervalTime * dScroll * AUTOCHANGE_INTERVAL_SCALE;
                }
            }
            if (intervalTime < MIN_INTERVAL_TIME) {
                intervalTime = MIN_INTERVAL_TIME;
            }
            Logs.info("Nowy interwał autoprzewijania: " + intervalTime + " ms");
        }
    }

    @Override
    public void onEvent(IEvent event) {
        if (event instanceof AutoscrollStartRequestEvent) {
            start();
        } else if (event instanceof AutoscrollStopRequestEvent) {
            stop();
        }
    }
}
