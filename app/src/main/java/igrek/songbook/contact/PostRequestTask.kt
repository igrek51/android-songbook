package igrek.songbook.contact

import android.os.AsyncTask
import com.google.common.base.Joiner
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.*
import javax.net.ssl.HttpsURLConnection

class PostRequestTask(private val url: String, private val postParams: Map<String, String>, private val responseConsumer: ResponseConsumer<String>?, private val errorConsumer: ResponseConsumer<Throwable>) : AsyncTask<String, String, String>() {

    override fun doInBackground(vararg argParams: String): String? {
        try {
            val postDataString = getPostDataString(postParams)
            // set up connection
            val urlConnection = URL(url).openConnection() as HttpURLConnection
            urlConnection.requestMethod = "POST"
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            urlConnection.setRequestProperty("Content-Length", Integer.toString(postDataString.toByteArray(charset("UTF-8")).size))
            urlConnection.setRequestProperty("Accept", "*/*")
            urlConnection.setRequestProperty("User-Agent", "curl/7.52.1")
            urlConnection.useCaches = false
            urlConnection.doInput = true
            urlConnection.doOutput = true
            urlConnection.setChunkedStreamingMode(0)

            // send request
            val out = BufferedOutputStream(urlConnection.outputStream)
            val writer = BufferedWriter(OutputStreamWriter(out, "UTF-8"))
            writer.write(postDataString)
            writer.flush()
            writer.close()
            out.close()
            urlConnection.connect()

            // get response
            val response = StringBuilder()
            val responseCode = urlConnection.responseCode
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                var line: String
                val br = BufferedReader(InputStreamReader(urlConnection.inputStream))
                while (true) {
                    line = br.readLine()
                    if (line == null)
                        break
                    response.append(line)
                }
            }
            return response.toString()
        } catch (e: Throwable) {
            errorConsumer.accept(e)
            return null
        }

    }

    override fun onPostExecute(response: String?) {
        if (response != null && responseConsumer != null)
            responseConsumer.accept(response)
    }

    @Throws(UnsupportedEncodingException::class)
    private fun getPostDataString(params: Map<String, String>): String {
        val pairs = ArrayList<String>()
        // WTF ?!? Django skips first arg
        pairs.add("fuckingDjangoWTF=1")
        for ((key, value) in params) {
            val pair = URLEncoder.encode(key, "UTF-8") + "=" + URLEncoder.encode(value, "UTF-8")
            pairs.add(pair)
        }
        pairs.add("wtfFuckinDjango=2")
        return Joiner.on("&").join(pairs)
    }
}