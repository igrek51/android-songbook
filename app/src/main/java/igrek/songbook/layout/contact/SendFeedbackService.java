package igrek.songbook.layout.contact;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;

import java.io.IOException;

import javax.inject.Inject;

import igrek.songbook.R;
import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.info.UiInfoService;
import igrek.songbook.info.UiResourceService;
import igrek.songbook.info.logger.Logger;
import igrek.songbook.info.logger.LoggerFactory;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SendFeedbackService {
	
	@Inject
	UiInfoService uiInfoService;
	@Inject
	Activity activity;
	@Inject
	UiResourceService uiResourceService;
	@Inject
	OkHttpClient okHttpClient;
	
	private static final int APPLICATION_ID = 1;
	private static final String url = "http://51.38.128.10:8006/contact/send/";
	
	private Logger logger = LoggerFactory.getLogger();
	
	public SendFeedbackService() {
		DaggerIoc.getFactoryComponent().inject(this);
	}
	
	public void sendFeedback(String message, String author) {
		uiInfoService.showInfo(uiResourceService.resString(R.string.contact_sending));
		
		RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
				.addFormDataPart("message", message)
				.addFormDataPart("author", author)
				.addFormDataPart("application_id", Integer.toString(APPLICATION_ID))
				.build();
		
		Request request = new Request.Builder().url(url).post(requestBody).build();
		
		okHttpClient.newCall(request).enqueue(new Callback() {
			@Override
			public void onFailure(Call call, IOException e) {
				onErrorReceived(e.getMessage());
			}
			
			@Override
			public void onResponse(Call call, final Response response) throws IOException {
				if (!response.isSuccessful()) {
					onErrorReceived("Unexpected code: " + response);
				} else {
					onResponseReceived(response.body().string());
				}
			}
		});
	}
	
	private void onResponseReceived(String response) {
		logger.debug("Feedback sent response: " + response);
		new Handler(Looper.getMainLooper()).post(() -> {
			if (response.startsWith("200")) {
				uiInfoService.showInfo(R.string.contact_message_sent_successfully);
			} else {
				onErrorReceived("Feedback sent bad response: " + response);
			}
		});
	}
	
	private void onErrorReceived(String errorMessage) {
		logger.error("Feedback sending error: " + errorMessage);
		new Handler(Looper.getMainLooper()).post(() -> {
			uiInfoService.showInfoIndefinite(R.string.contact_error_sending);
		});
	}
	
}
