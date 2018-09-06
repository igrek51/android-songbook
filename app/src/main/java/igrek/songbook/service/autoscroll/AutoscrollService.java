package igrek.songbook.service.autoscroll;

import android.os.Handler;

import javax.inject.Inject;

import dagger.Lazy;
import igrek.songbook.R;
import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.logger.Logger;
import igrek.songbook.logger.LoggerFactory;
import igrek.songbook.service.info.UiInfoService;
import igrek.songbook.service.info.UiResourceService;
import igrek.songbook.service.layout.songpreview.SongPreviewLayoutController;
import igrek.songbook.service.preferences.PreferencesDefinition;
import igrek.songbook.service.preferences.PreferencesService;
import igrek.songbook.view.songpreview.CanvasGraphics;

public class AutoscrollService {
	
	private final float MIN_SPEED = 0.001f;
	private final float START_NO_WAITING_MIN_SCROLL = 24.0f;
	private final float AUTOCHANGE_SPEED_SCALE = 0.00025f;
	private final float AUTOCHANGE_MAX_SCROLL = 150f; // [px]
	private final float AUTOCHANGE_WAITING_SCALE = 9.0f;
	private final float AUTOSCROLL_INTERVAL_TIME = 100; // [ms]
	@Inject
	UiInfoService userInfo;
	@Inject
	Lazy<SongPreviewLayoutController> songPreviewController;
	@Inject
	UiResourceService uiResourceService;
	@Inject
	PreferencesService preferencesService;
	
	private Logger logger = LoggerFactory.getLogger();
	private AutoscrollState state;
	private long initialPause; // [ms]
	private float autoscrollSpeed; // [em / s]
	private long startTime; // [ms]
	
	private Handler timerHandler = new Handler();
	private Runnable timerRunnable = () -> {
		if (state == AutoscrollState.OFF)
			return;
		handleAutoscrollStep();
	};
	
	public AutoscrollService() {
		DaggerIoc.getFactoryComponent().inject(this);
		loadPreferences();
		reset();
	}
	
	private void loadPreferences() {
		initialPause = preferencesService.getValue(PreferencesDefinition.autoscrollInitialPause, Long.class);
		autoscrollSpeed = preferencesService.getValue(PreferencesDefinition.autoscrollSpeed, Float.class);
	}
	
	public void reset() {
		stop();
	}
	
	public void start() {
		float scroll = getCanvas().getScroll();
		if (scroll <= START_NO_WAITING_MIN_SCROLL) {
			start(true);
		} else {
			start(false);
		}
	}
	
	private void start(boolean withWaiting) {
		if (isRunning()) {
			stop();
		}
		if (getCanvas().canScrollDown()) {
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
	
	private CanvasGraphics getCanvas() {
		return songPreviewController.get().getCanvas();
	}
	
	private void handleAutoscrollStep() {
		if (state == AutoscrollState.WAITING) {
			long remainingTimeMs = initialPause + startTime - System.currentTimeMillis();
			if (remainingTimeMs <= 0) {
				state = AutoscrollState.SCROLLING;
				timerHandler.postDelayed(timerRunnable, 0);
				onAutoscrollStartedEvent();
			} else {
				long delay = remainingTimeMs > 1000 ? 1000 : remainingTimeMs; // cut off over 1000
				timerHandler.postDelayed(timerRunnable, delay);
				onAutoscrollRemainingWaitTimeEvent(remainingTimeMs);
			}
		} else if (state == AutoscrollState.SCROLLING) {
			// em = speed * time
			float lineheightPart = autoscrollSpeed * AUTOSCROLL_INTERVAL_TIME / 1000;
			if (getCanvas().scrollBy(lineheightPart)) {
				// scroll once again later
				timerHandler.postDelayed(timerRunnable, (long) AUTOSCROLL_INTERVAL_TIME);
			} else {
				// scroll has come to an end
				stop();
				onAutoscrollEndedEvent();
			}
		}
	}
	
	public void onCanvasScrollEvent(float dScroll, float scroll) {
		if (state == AutoscrollState.WAITING) {
			if (dScroll > 0) { // skip counting down immediately
				state = AutoscrollState.SCROLLING;
				onAutoscrollStartedEvent();
			} else if (dScroll < 0) { // increase inital waitng time
				startTime -= (long) (dScroll * AUTOCHANGE_WAITING_SCALE);
				long remainingTimeMs = initialPause + startTime - System.currentTimeMillis();
				onAutoscrollRemainingWaitTimeEvent(remainingTimeMs);
			}
		} else if (state == AutoscrollState.SCROLLING) {
			if (dScroll > 0) { // speed up scrolling
				
				dScroll = dScroll > AUTOCHANGE_MAX_SCROLL ? AUTOCHANGE_MAX_SCROLL : dScroll;
				autoscrollSpeed += dScroll * AUTOCHANGE_SPEED_SCALE;
				
			} else if (dScroll < 0) {
				if (scroll <= 0) { // scrolling up to the beginning
					// set counting down state with additional time
					state = AutoscrollState.WAITING;
					startTime = System.currentTimeMillis() - initialPause - (long) (dScroll * AUTOCHANGE_WAITING_SCALE);
					long remainingTimeMs = initialPause + startTime - System.currentTimeMillis();
					onAutoscrollRemainingWaitTimeEvent(remainingTimeMs);
					return;
				} else {
					// slow down scrolling
					float dScrollAbs = -dScroll;
					dScrollAbs = dScrollAbs > AUTOCHANGE_MAX_SCROLL ? AUTOCHANGE_MAX_SCROLL : dScrollAbs;
					autoscrollSpeed -= dScrollAbs * AUTOCHANGE_SPEED_SCALE;
				}
			}
			if (autoscrollSpeed < MIN_SPEED)
				autoscrollSpeed = MIN_SPEED;
			logger.info("new autoscroll speed: " + autoscrollSpeed + " em / s");
		}
	}
	
	private void onAutoscrollRemainingWaitTimeEvent(long ms) {
		int seconds = (int) ((ms + 500) / 1000);
		
		String info = uiResourceService.resString(R.string.autoscroll_starts_in) + " " + seconds + " s.";
		userInfo.showInfoWithAction(info, R.string.stop_autoscroll, this::stop);
	}
	
	public void onAutoscrollStartUIEvent() {
		if (!isRunning()) {
			if (getCanvas().canScrollDown()) {
				start();
				userInfo.showInfoWithAction(R.string.autoscroll_started, R.string.stop_autoscroll, this::stop);
			} else {
				userInfo.showInfo(uiResourceService.resString(R.string.end_of_file) + "\n" + uiResourceService
						.resString(R.string.autoscroll_not_started));
			}
		} else {
			onAutoscrollStopUIEvent();
		}
	}
	
	private void onAutoscrollStartedEvent() {
		userInfo.showInfoWithAction(R.string.autoscroll_started, R.string.stop_autoscroll, this::stop);
	}
	
	private void onAutoscrollEndedEvent() {
		userInfo.showInfo(uiResourceService.resString(R.string.end_of_file) + "\n" + uiResourceService
				.resString(R.string.autoscroll_stopped));
	}
	
	public void onAutoscrollStopUIEvent() {
		if (isRunning()) {
			stop();
			userInfo.showInfo(R.string.autoscroll_stopped);
		}
	}
	
	public long getInitialPause() {
		return initialPause;
	}
	
	public float getAutoscrollSpeed() {
		return autoscrollSpeed;
	}
	
}
