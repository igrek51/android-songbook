package igrek.songbook.service.autoscroll;

import android.os.Handler;

import javax.inject.Inject;

import igrek.songbook.R;
import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.logger.Logger;
import igrek.songbook.logger.LoggerFactory;
import igrek.songbook.service.info.UserInfoService;

public class AutoscrollService {

	private Logger logger = LoggerFactory.getLogger();

	private AutoscrollState state;

	private long waitTime = 35000; // [ms]
	private float intervalTime = 320; // [ms]
	private float intervalStep = 2.0f; // [px]

	private float fontsize;

	private long startTime; // [ms]

	private final long MIN_INTERVAL_TIME = 5;
	private final float START_NO_WAITING_MIN_SCROLL_FACTOR = 1.0f;

	private final float AUTOCHANGE_INTERVAL_SCALE = 0.0022f;
	private final float AUTOCHANGE_WAITING_SCALE = 9.0f;

	private final float MANUAL_SCROLL_MAX_RANGE = 150f;

	private Handler timerHandler;
	private Runnable timerRunnable;

	@Inject
	UserInfoService userInfo;

	public AutoscrollService() {
		DaggerIoc.getFactoryComponent().inject(this);

		timerHandler = new Handler();
//		TODO
//		timerRunnable = () -> {
//			if (state == AutoscrollState.OFF)
//				return;
//			handleAutoscrollStep();
//		};
//
//		fontsize = chordsManager.getFontsize();
//
//		reset();
	}

//	TODO
//	public void reset() {
//		stop();
//	}
//
//	public void start() {
//		CanvasGraphics canvas = AppController.getService(CanvasGraphics.class);
//		float scroll = canvas.getScroll();
//		if (scroll <= START_NO_WAITING_MIN_SCROLL_FACTOR * fontsize) {
//			start(true);
//		} else {
//			start(false);
//		}
//	}
//
//	private void start(boolean withWaiting) {
//		if (isRunning()) {
//			stop();
//		}
//		CanvasGraphics canvas = AppController.getService(CanvasGraphics.class);
//		if (canvas.canAutoScroll()) {
//			if (withWaiting) {
//				state = AutoscrollState.WAITING;
//			} else {
//				state = AutoscrollState.SCROLLING;
//			}
//			startTime = System.currentTimeMillis();
//			timerHandler.postDelayed(timerRunnable, 0);
//		}
//	}
//
//	public void stop() {
//		state = AutoscrollState.OFF;
//		timerHandler.removeCallbacks(timerRunnable);
//	}
//
//	public void toggle() {
//		if (isRunning()) {
//			stop();
//		} else {
//			start();
//		}
//	}
//
	public boolean isRunning() {
		return state == AutoscrollState.WAITING || state == AutoscrollState.SCROLLING;
	}
//
//	private void handleAutoscrollStep() {
//		if (state == AutoscrollState.WAITING) {
//			long remainingTimeMs = waitTime + startTime - System.currentTimeMillis();
//			if (remainingTimeMs <= 0) {
//				state = AutoscrollState.SCROLLING;
//				timerHandler.postDelayed(timerRunnable, 0);
//				AppController.sendEvent(new AutoscrollStartedEvent());
//			} else {
//				long delay = remainingTimeMs > 1000 ? 1000 : remainingTimeMs; //nasycenie do 1000
//				timerHandler.postDelayed(timerRunnable, delay);
//				AppController.sendEvent(new AutoscrollRemainingWaitTimeEvent(remainingTimeMs));
//			}
//		} else if (state == AutoscrollState.SCROLLING) {
//			CanvasGraphics canvas = AppController.getService(CanvasGraphics.class);
//			if (canvas.autoscrollBy(intervalStep)) {
//				timerHandler.postDelayed(timerRunnable, (long) intervalTime);
//			} else {
//				stop();
//				AppController.sendEvent(new AutoscrollEndedEvent());
//			}
//		}
//	}
//
//	public void setFontsize(float fontsize) {
//		//skalowanie czcionki zmienia skaluje intervał / step
//		intervalTime = intervalTime * this.fontsize / fontsize;
//		if (intervalTime < MIN_INTERVAL_TIME) {
//			intervalTime = MIN_INTERVAL_TIME;
//		}
//		this.fontsize = fontsize;
//		Logger.info("Nowy interwał autoprzewijania (po zmianie czcionki): " + intervalTime + " ms");
//	}
//
//	public void handleCanvasScroll(float dScroll, float scroll) {
//
//		if (state == AutoscrollState.WAITING) {
//			if (dScroll > 0) { //natychmiastowe pominięcie odliczania
//				state = AutoscrollState.SCROLLING;
//				AppController.sendEvent(new AutoscrollStartedEvent());
//			} else if (dScroll < 0) { //wydłużenie czasu odliczania
//				startTime -= (long) (dScroll * AUTOCHANGE_WAITING_SCALE);
//				long remainingTimeMs = waitTime + startTime - System.currentTimeMillis();
//				AppController.sendEvent(new AutoscrollRemainingWaitTimeEvent(remainingTimeMs));
//			}
//		} else if (state == AutoscrollState.SCROLLING) {
//			if (dScroll > 0) { //przyspieszanie tempa autoprzewijania
//
//				if (dScroll > MANUAL_SCROLL_MAX_RANGE) {
//					dScroll = MANUAL_SCROLL_MAX_RANGE;
//				}
//				intervalTime -= intervalTime * dScroll * AUTOCHANGE_INTERVAL_SCALE;
//
//
//			} else if (dScroll < 0) {
//				if (scroll <= 0) { //przewinięcie na początek pliku
//					//przejście w tryb odliczania z dodaniem czasu
//					state = AutoscrollState.WAITING;
//					startTime = System.currentTimeMillis() - waitTime - (long) (dScroll * AUTOCHANGE_WAITING_SCALE);
//					long remainingTimeMs = waitTime + startTime - System.currentTimeMillis();
//					AppController.sendEvent(new AutoscrollRemainingWaitTimeEvent(remainingTimeMs));
//					return;
//				} else {
//					//zwalnianie tempa autoprzewijania
//
//					if (dScroll < -MANUAL_SCROLL_MAX_RANGE) {
//						dScroll = -MANUAL_SCROLL_MAX_RANGE;
//					}
//
//					intervalTime -= intervalTime * dScroll * AUTOCHANGE_INTERVAL_SCALE;
//
//				}
//			}
//			if (intervalTime < MIN_INTERVAL_TIME) {
//				intervalTime = MIN_INTERVAL_TIME;
//			}
//			logger.info("Nowy interwał autoprzewijania: " + intervalTime + " ms");
//		}
//	}

//	TODO
//	@Override
//	public void onEvent(IEvent event) {
//		if (event instanceof AutoscrollStartEvent) {
//			start();
//		} else if (event instanceof AutoscrollStopEvent) {
//			stop();
//		} else if (event instanceof AutoscrollRemainingWaitTimeEvent) {
//
//			long ms = ((AutoscrollRemainingWaitTimeEvent) event).getMs();
//
//			int seconds = (int) ((ms + 500) / 1000);
//
//			userInfo.showActionInfo(userInfo.resString(R.string.autoscroll_starts_in) + " " + seconds + " s.", null, userInfo
//					.resString(R.string.stop_autoscroll), new InfoBarClickAction() {
//				@Override
//				public void onClick() {
//					AppController.sendEvent(new AutoscrollStopEvent());
//				}
//			});
//
//		} else if (event instanceof AutoscrollStartUIEvent) {
//
//			if (!isRunning()) {
//				CanvasGraphics canvas = AppController.getService(CanvasGraphics.class);
//				if (canvas.canAutoScroll()) {
//					AppController.sendEvent(new AutoscrollStartEvent());
//
//					userInfo.showActionInfo(R.string.autoscroll_started, null, userInfo.resString(R.string.stop_autoscroll), new InfoBarClickAction() {
//						@Override
//						public void onClick() {
//							AppController.sendEvent(new AutoscrollStopEvent());
//						}
//					});
//				} else {
//					userInfo.showActionInfo(userInfo.resString(R.string.end_of_file) + "\n" + userInfo
//							.resString(R.string.autoscroll_not_started), null, userInfo.resString(R.string.action_info_ok), null);
//				}
//			} else {
//				AppController.sendEvent(new AutoscrollStopUIEvent());
//			}
//
//		} else if (event instanceof AutoscrollStartedEvent) {
//
//			userInfo.showActionInfo(R.string.autoscroll_started, null, userInfo.resString(R.string.stop_autoscroll), new InfoBarClickAction() {
//				@Override
//				public void onClick() {
//					AppController.sendEvent(new AutoscrollStopEvent());
//				}
//			});
//
//		} else if (event instanceof AutoscrollEndedEvent) {
//
//			userInfo.showActionInfo(userInfo.resString(R.string.end_of_file) + "\n" + userInfo.resString(R.string.autoscroll_stopped), null, userInfo
//					.resString(R.string.action_info_ok), null);
//
//		} else if (event instanceof AutoscrollStopUIEvent) {
//
//			if (isRunning()) {
//				AppController.sendEvent(new AutoscrollStopEvent());
//				userInfo.showActionInfo(R.string.autoscroll_stopped, null, userInfo.resString(R.string.action_info_ok), null);
//			}
//
//		} else if (event instanceof CanvasScrollEvent) {
//
//			float dScroll = ((CanvasScrollEvent) event).getdScroll();
//			float scroll = ((CanvasScrollEvent) event).getScroll();
//
//			handleCanvasScroll(dScroll, scroll);
//
//		}
//	}
}
