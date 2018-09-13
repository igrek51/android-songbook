package igrek.songbook.service.layout.contact;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.logger.Logger;
import igrek.songbook.logger.LoggerFactory;
import igrek.songbook.service.info.UiInfoService;

public class SendFeedbackService {
	
	@Inject
	UiInfoService uiInfoService;
	@Inject
	Activity activity;
	
	private static final int APPLICATION_ID = 1;
	private static final String url = "http://51.38.128.10:8006/contact/send/";
	
	private Logger logger = LoggerFactory.getLogger();
	
	public SendFeedbackService() {
		DaggerIoc.getFactoryComponent().inject(this);
	}
	
	public void sendFeedback(String message, String author) {
		uiInfoService.showToast("Sending...");
		
		Map<String, String> params = new HashMap<>();
		params.put("message", message);
		params.put("author", author);
		params.put("application_id", Integer.toString(APPLICATION_ID));
		
		new PostRequestTask(url, params, response -> {
			onResponseReceived(response);
		}, error -> {
			onErrorReceived(error);
		}).execute();
	}
	
	private void onResponseReceived(String response) {
		logger.debug("HTTP response: " + response);
		new Handler(Looper.getMainLooper()).post(() -> {
			uiInfoService.showToast("Your message has been sent. Thanks :)");
		});
	}
	
	private void onErrorReceived(Throwable error) {
		logger.error(error.getMessage());
		new Handler(Looper.getMainLooper()).post(() -> {
			uiInfoService.showToast("Sorry, an error has occurred while sending your message :(");
		});
	}
	
}
