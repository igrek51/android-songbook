package igrek.songbook.layout.contact;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import igrek.songbook.R;
import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.info.UiInfoService;
import igrek.songbook.info.UiResourceService;
import igrek.songbook.info.logger.Logger;
import igrek.songbook.info.logger.LoggerFactory;

public class SendFeedbackService {
	
	@Inject
	UiInfoService uiInfoService;
	@Inject
	Activity activity;
	@Inject
	UiResourceService uiResourceService;
	
	private static final int APPLICATION_ID = 1;
	private static final String url = "http://51.38.128.10:8006/contact/send/";
	
	private Logger logger = LoggerFactory.getLogger();
	
	public SendFeedbackService() {
		DaggerIoc.getFactoryComponent().inject(this);
	}
	
	public void sendFeedback(String message, String author) {
		uiInfoService.showInfo(uiResourceService.resString(R.string.contact_sending));
		
		Map<String, String> params = new HashMap<>();
		params.put("message", message);
		params.put("author", author);
		params.put("application_id", Integer.toString(APPLICATION_ID));
		
		new Handler().post(() -> new PostRequestTask(url, params, response -> {
			onResponseReceived(response);
		}, error -> {
			onErrorReceived(error);
		}).execute());
	}
	
	private void onResponseReceived(String response) {
		logger.debug("Feedback sent response: " + response);
		new Handler(Looper.getMainLooper()).post(() -> {
			if (response.startsWith("200")) {
				uiInfoService.showInfo(uiResourceService.resString(R.string.contact_message_sent_successfully));
			} else {
				logger.error("Feedback sent bad response: " + response);
				uiInfoService.showInfoIndefinite(uiResourceService.resString(R.string.contact_error_sending));
			}
		});
	}
	
	private void onErrorReceived(Throwable error) {
		logger.error("Feedback sending error: " + error.getMessage());
		new Handler(Looper.getMainLooper()).post(() -> {
			uiInfoService.showInfoIndefinite(uiResourceService.resString(R.string.contact_error_sending));
		});
	}
	
}
