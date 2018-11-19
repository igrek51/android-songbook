package igrek.songbook.songpreview.autoscroll;

import android.annotation.SuppressLint;
import android.os.Handler;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import dagger.Lazy;
import igrek.songbook.R;
import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.info.UiInfoService;
import igrek.songbook.info.UiResourceService;
import igrek.songbook.info.logger.Logger;
import igrek.songbook.info.logger.LoggerFactory;
import igrek.songbook.persistence.preferences.PreferencesDefinition;
import igrek.songbook.persistence.preferences.PreferencesService;
import igrek.songbook.songpreview.SongPreviewLayoutController;
import igrek.songbook.songpreview.renderer.SongPreview;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subjects.PublishSubject;

@SuppressLint("CheckResult")
public class AutoscrollService {
	
	private final float MIN_SPEED = 0.001f; // [line / s]
	private final float START_NO_WAITING_MIN_SCROLL = 0.9f; // [line]
	private final float ADJUSTMENT_SPEED_SCALE = 0.0008f; // [line / s  /  scrolled lines]
	private final float ADJUSTMENT_MAX_SPEED_CHANGE = 0.03f; // [line / s  /  scrolled lines]
	private final float ADD_INITIAL_PAUSE_SCALE = 180.0f; // [ms  /  scrolled lines]
	private final float AUTOSCROLL_INTERVAL_TIME = 60; // [ms]
	@Inject
	UiInfoService uiInfoService;
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
	private float scrolledBuffer = 0;
	
	private PublishSubject<Float> canvasScrollSubject = PublishSubject.create();
	private PublishSubject<Float> aggregatedScrollSubject = PublishSubject.create();
	private PublishSubject<AutoscrollState> scrollStateSubject = PublishSubject.create();
	private PublishSubject<Float> scrollSpeedAdjustmentSubject = PublishSubject.create();
	
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
		
		// aggreagate many little scrolls into greater parts (not proper RX method found)
		canvasScrollSubject.observeOn(AndroidSchedulers.mainThread())
				.subscribe(linePartScrolled -> {
					scrolledBuffer += linePartScrolled;
					aggregatedScrollSubject.onNext(scrolledBuffer);
				});
		
		aggregatedScrollSubject.throttleLast(300, TimeUnit.MILLISECONDS)
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(scrolled -> {
					onCanvasScrollEvent(scrolledBuffer, getCanvas().getScroll());
					scrolledBuffer = 0;
				});
	}
	
	private void loadPreferences() {
		initialPause = preferencesService.getValue(PreferencesDefinition.autoscrollInitialPause, Long.class);
		autoscrollSpeed = preferencesService.getValue(PreferencesDefinition.autoscrollSpeed, Float.class);
	}
	
	public void reset() {
		stop();
	}
	
	public void start() {
		float linePartScroll = getCanvas().getScroll() / getCanvas().getLineheightPx();
		if (linePartScroll <= START_NO_WAITING_MIN_SCROLL) {
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
		scrollStateSubject.onNext(state);
	}
	
	public void stop() {
		state = AutoscrollState.OFF;
		timerHandler.removeCallbacks(timerRunnable);
		scrollStateSubject.onNext(state);
	}
	
	public boolean isRunning() {
		return state == AutoscrollState.WAITING || state == AutoscrollState.SCROLLING;
	}
	
	private SongPreview getCanvas() {
		return songPreviewController.get().getSongPreview();
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
			if (getCanvas().scrollByLines(lineheightPart)) {
				// scroll once again later
				timerHandler.postDelayed(timerRunnable, (long) AUTOSCROLL_INTERVAL_TIME);
			} else {
				// scroll has come to an end
				stop();
				onAutoscrollEndedEvent();
			}
		}
	}
	
	/**
	 * @param dScroll line Part Scrolled
	 * @param scroll  current scroll
	 */
	private void onCanvasScrollEvent(float dScroll, float scroll) {
		if (state == AutoscrollState.WAITING) {
			if (dScroll > 0) { // skip counting down immediately
				skipInitialPause();
			} else if (dScroll < 0) { // increase inital waitng time
				startTime -= (long) (dScroll * ADD_INITIAL_PAUSE_SCALE);
				long remainingTimeMs = initialPause + startTime - System.currentTimeMillis();
				onAutoscrollRemainingWaitTimeEvent(remainingTimeMs);
			}
		} else if (state == AutoscrollState.SCROLLING) {
			if (dScroll > 0) { // speed up scrolling
				
				autoscrollSpeed += cutOffMax(dScroll * ADJUSTMENT_SPEED_SCALE, ADJUSTMENT_MAX_SPEED_CHANGE);
				
			} else if (dScroll < 0) {
				if (scroll <= 0) { // scrolling up to the beginning
					// set counting down state with additional time
					state = AutoscrollState.WAITING;
					startTime = System.currentTimeMillis() - initialPause - (long) (dScroll * ADD_INITIAL_PAUSE_SCALE);
					long remainingTimeMs = initialPause + startTime - System.currentTimeMillis();
					onAutoscrollRemainingWaitTimeEvent(remainingTimeMs);
					return;
				} else {
					// slow down scrolling
					float dScrollAbs = -dScroll;
					autoscrollSpeed -= cutOffMax(dScrollAbs * ADJUSTMENT_SPEED_SCALE, ADJUSTMENT_MAX_SPEED_CHANGE);
				}
			}
			if (autoscrollSpeed < MIN_SPEED)
				autoscrollSpeed = MIN_SPEED;
			
			scrollSpeedAdjustmentSubject.onNext(autoscrollSpeed);
			logger.info("autoscroll speed adjustment: " + autoscrollSpeed + " line / s");
		}
	}
	
	private float cutOffMax(float value, float max) {
		return value > max ? max : value;
	}
	
	private void onAutoscrollRemainingWaitTimeEvent(long ms) {
		String seconds = Long.toString((ms + 500) / 1000);
		String info = uiResourceService.resString(R.string.autoscroll_starts_in, seconds);
		uiInfoService.showInfoWithAction(info, R.string.action_start_now_autoscroll, this::skipInitialPause);
	}
	
	private void skipInitialPause() {
		state = AutoscrollState.SCROLLING;
		uiInfoService.clearSnackBars();
		onAutoscrollStartedEvent();
	}
	
	private void onAutoscrollStartUIEvent() {
		if (!isRunning()) {
			if (getCanvas().canScrollDown()) {
				start();
				uiInfoService.showInfoWithAction(R.string.autoscroll_started, R.string.action_stop_autoscroll, this::stop);
			} else {
				uiInfoService.showInfo(uiResourceService.resString(R.string.end_of_song_autoscroll_stopped));
			}
		} else {
			onAutoscrollStopUIEvent();
		}
	}
	
	private void onAutoscrollStartedEvent() {
		uiInfoService.showInfoWithAction(R.string.autoscroll_started, R.string.action_stop_autoscroll, this::stop);
	}
	
	private void onAutoscrollEndedEvent() {
		uiInfoService.showInfo(uiResourceService.resString(R.string.end_of_song_autoscroll_stopped));
	}
	
	public void onAutoscrollStopUIEvent() {
		if (isRunning()) {
			stop();
			uiInfoService.showInfo(R.string.autoscroll_stopped);
		}
	}
	
	public void onAutoscrollToggleUIEvent() {
		if (isRunning()) {
			onAutoscrollStopUIEvent();
		} else {
			onAutoscrollStartUIEvent();
		}
	}
	
	public long getInitialPause() {
		return initialPause;
	}
	
	public float getAutoscrollSpeed() {
		return autoscrollSpeed;
	}
	
	public void setInitialPause(long initialPause) {
		this.initialPause = initialPause;
	}
	
	public void setAutoscrollSpeed(float autoscrollSpeed) {
		this.autoscrollSpeed = autoscrollSpeed;
	}
	
	public PublishSubject<Float> getCanvasScrollSubject() {
		return canvasScrollSubject;
	}
	
	public PublishSubject<AutoscrollState> getScrollStateSubject() {
		return scrollStateSubject;
	}
	
	public PublishSubject<Float> getScrollSpeedAdjustmentSubject() {
		return scrollSpeedAdjustmentSubject;
	}
}
