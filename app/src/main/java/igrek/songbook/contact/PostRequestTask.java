package igrek.songbook.contact;

import android.os.AsyncTask;

import com.google.common.base.Joiner;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import igrek.songbook.info.logger.Logger;
import igrek.songbook.info.logger.LoggerFactory;

public class PostRequestTask extends AsyncTask<String, String, String> {
	
	private ResponseConsumer<String> responseConsumer;
	private ResponseConsumer<Throwable> errorConsumer;
	private String url;
	private Map<String, String> postParams;
	private Logger logger = LoggerFactory.INSTANCE.getLogger();
	
	public PostRequestTask(String url, Map<String, String> postParams, ResponseConsumer<String> responseConsumer, ResponseConsumer<Throwable> errorConsumer) {
		this.url = url;
		this.postParams = postParams;
		this.responseConsumer = responseConsumer;
		this.errorConsumer = errorConsumer;
	}
	
	@Override
	protected String doInBackground(String... argParams) {
		try {
			String postDataString = getPostDataString(postParams);
			// set up connection
			HttpURLConnection urlConnection = (HttpURLConnection) new URL(url).openConnection();
			urlConnection.setRequestMethod("POST");
			urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			urlConnection.setRequestProperty("Content-Length", Integer.toString(postDataString.getBytes("UTF-8").length));
			urlConnection.setRequestProperty("Accept", "*/*");
			urlConnection.setRequestProperty("User-Agent", "curl/7.52.1");
			urlConnection.setUseCaches(false);
			urlConnection.setDoInput(true);
			urlConnection.setDoOutput(true);
			urlConnection.setChunkedStreamingMode(0);
			
			// send request
			OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
			writer.write(postDataString);
			writer.flush();
			writer.close();
			out.close();
			urlConnection.connect();
			
			// get response
			StringBuilder response = new StringBuilder();
			int responseCode = urlConnection.getResponseCode();
			if (responseCode == HttpsURLConnection.HTTP_OK) {
				String line;
				BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
				while ((line = br.readLine()) != null) {
					response.append(line);
				}
			}
			return response.toString();
		} catch (Throwable e) {
			errorConsumer.accept(e);
			return null;
		}
	}
	
	@Override
	protected void onPostExecute(String response) {
		if (response != null && responseConsumer != null)
			responseConsumer.accept(response);
	}
	
	private String getPostDataString(Map<String, String> params) throws UnsupportedEncodingException {
		List<String> pairs = new ArrayList<>();
		// WTF ?!? Django skips first arg
		pairs.add("fuckingDjangoWTF=1");
		for (Map.Entry<String, String> entry : params.entrySet()) {
			String pair = URLEncoder.encode(entry.getKey(), "UTF-8") + "=" + URLEncoder.encode(entry
					.getValue(), "UTF-8");
			pairs.add(pair);
		}
		pairs.add("wtfFuckinDjango=2");
		return Joiner.on("&").join(pairs);
	}
}