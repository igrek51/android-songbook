package igrek.songbook.service.autoscroll;

import android.os.Handler;

import javax.inject.Inject;

import dagger.Lazy;
import igrek.songbook.R;
import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.logger.Logger;
import igrek.songbook.logger.LoggerFactory;
import igrek.songbook.service.chords.ChordsManager;
import igrek.songbook.service.info.UiInfoService;
import igrek.songbook.service.info.UiResourceService;
import igrek.songbook.service.layout.songpreview.SongPreviewController;
import igrek.songbook.view.songpreview.CanvasGraphics;

public class AutoscrollService {
	
	private final long MIN_INTERVAL_TIME = 5;
	private final float START_NO_WAITING_MIN_SCROLL_FACTOR = 1.0f;
	private final float AUTOCHANGE_INTERVAL_SCALE = 0.0022f;
	private final float AUTOCHANGE_WAITING_SCALE = 9.0f;
	private final float MANUAL_SCROLL_MAX_RANGE = 150f;
	@Inject
	UiInfoService userInfo;
	@Inject
	Lazy<ChordsManager> chordsManager;
	@Inject
	Lazy<SongPreviewController> songPreviewController;
	@Inject
	UiResourceService uiResourceService;
	
	private Logger logger = LoggerFactory.getLogger();
	private AutoscrollState state;
	private long waitTime = 35000; // [ms]
	private float intervalTime = 320; // [ms]
	private float intervalStep = 2.0f; // [px]
	private float fontsize;
	private long startTime; // [ms]
	private Handler timerHandler;
	private Runnable timerRunnable;
	
	public AutoscrollService() {
		DaggerIoc.getFactoryComponent().inject(this);
		
		timerHandler = new Handler();
		timerRunnable = () -> {
			if (state == AutoscrollState.OFF)
				return;
			handleAutoscrollStep();
		};
		
		fontsize = chordsManager.get().getFontsize();
		
		reset();
	}
	
	public void reset() {
		stop();
	}
	
	public void start() {
		float scroll = getCanvas().getScroll();
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
		if (getCanvas().canAutoScroll()) {
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
	
	public void toggle() {
		if (isRunning()) {
			stop();
		} else {
			start();
		}
	}
	
	public boolean isRunning() {
		return state == AutoscrollState.WAITING || state == AutoscrollState.SCROLLING;
	}
	
	private CanvasGraphics getCanvas() {
		return songPreviewController.get().getCanvas();
	}
	
	private void handleAutoscrollStep() {
		if (state == AutoscrollState.WAITING) {
			long remainingTimeMs = waitTime + startTime - System.currentTimeMillis();
			if (remainingTimeMs <= 0) {
				state = AutoscrollState.SCROLLING;
				timerHandler.postDelayed(timerRunnable, 0);
				onAutoscrollStartedEvent();
			} else {
				long delay = remainingTimeMs > 1000 ? 1000 : remainingTimeMs; //nasycenie do 1000
				timerHandler.postDelayed(timerRunnable, delay);
				onAutoscrollRemainingWaitTimeEvent(remainingTimeMs);
			}
		} else if (state == AutoscrollState.SCROLLING) {
			if (getCanvas().autoscrollBy(intervalStep)) {
				timerHandler.postDelayed(timerRunnable, (long) intervalTime);
			} else {
				stop();
				onAutoscrollEndedEvent();
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
		logger.info("new autoscroll interval (fontsize change): " + intervalTime + " ms");
	}
	
	public void handleCanvasScroll(float dScroll, float scroll) {
		
		if (state == AutoscrollState.WAITING) {
			if (dScroll > 0) { //natychmiastowe pominięcie odliczania
				state = AutoscrollState.SCROLLING;
				onAutoscrollStartedEvent();
			} else if (dScroll < 0) { //wydłużenie czasu odliczania
				startTime -= (long) (dScroll * AUTOCHANGE_WAITING_SCALE);
				long remainingTimeMs = waitTime + startTime - System.currentTimeMillis();
				onAutoscrollRemainingWaitTimeEvent(remainingTimeMs);
			}
		} else if (state == AutoscrollState.SCROLLING) {
			if (dScroll > 0) { //przyspieszanie tempa autoprzewijania
				
				if (dScroll > MANUAL_SCROLL_MAX_RANGE) {
					dScroll = MANUAL_SCROLL_MAX_RANGE;
				}
				intervalTime -= intervalTime * dScroll * AUTOCHANGE_INTERVAL_SCALE;
				
				
			} else if (dScroll < 0) {
				if (scroll <= 0) { //przewinięcie na początek pliku
					//przejście w tryb odliczania z dodaniem czasu
					state = AutoscrollState.WAITING;
					startTime = System.currentTimeMillis() - waitTime - (long) (dScroll * AUTOCHANGE_WAITING_SCALE);
					long remainingTimeMs = waitTime + startTime - System.currentTimeMillis();
					onAutoscrollRemainingWaitTimeEvent(remainingTimeMs);
					return;
				} else {
					//zwalnianie tempa autoprzewijania
					
					if (dScroll < -MANUAL_SCROLL_MAX_RANGE) {
						dScroll = -MANUAL_SCROLL_MAX_RANGE;
					}
					
					intervalTime -= intervalTime * dScroll * AUTOCHANGE_INTERVAL_SCALE;
					
				}
			}
			if (intervalTime < MIN_INTERVAL_TIME) {
				intervalTime = MIN_INTERVAL_TIME;
			}
			logger.info("new autoscroll interval: " + intervalTime + " ms");
		}
	}
	
	
	public void onAutoscrollStartEvent() {
		start();
	}
	
	public void onAutoscrollStopEvent() {
		stop();
	}
	
	public void onAutoscrollRemainingWaitTimeEvent(long ms) {
		int seconds = (int) ((ms + 500) / 1000);
		
		String info = uiResourceService.resString(R.string.autoscroll_starts_in) + " " + seconds + " s.";
		userInfo.showInfoWithAction(info, R.string.stop_autoscroll, this::onAutoscrollStopEvent);
	}
	
	public void onAutoscrollStartUIEvent() {
		if (!isRunning()) {
			if (getCanvas().canAutoScroll()) {
				onAutoscrollStartEvent();
				userInfo.showInfoWithAction(R.string.autoscroll_started, R.string.stop_autoscroll, this::onAutoscrollStopEvent);
			} else {
				userInfo.showInfo(uiResourceService.resString(R.string.end_of_file) + "\n" + uiResourceService
						.resString(R.string.autoscroll_not_started));
			}
		} else {
			onAutoscrollStopUIEvent();
		}
	}
	
	public void onAutoscrollStartedEvent() {
		userInfo.showInfoWithAction(R.string.autoscroll_started, R.string.stop_autoscroll, this::onAutoscrollStopEvent);
	}
	
	public void onAutoscrollEndedEvent() {
		userInfo.showInfo(uiResourceService.resString(R.string.end_of_file) + "\n" + uiResourceService
				.resString(R.string.autoscroll_stopped));
	}
	
	public void onAutoscrollStopUIEvent() {
		if (isRunning()) {
			onAutoscrollStopEvent();
			userInfo.showInfo(R.string.autoscroll_stopped);
		}
	}
	
	public void onCanvasScrollEvent(float dScroll, float scroll) {
		handleCanvasScroll(dScroll, scroll);
	}
	
}
