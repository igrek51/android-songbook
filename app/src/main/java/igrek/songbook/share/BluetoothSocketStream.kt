package igrek.songbook.share

import android.bluetooth.BluetoothSocket
import android.os.SystemClock
import igrek.songbook.info.logger.LoggerFactory
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class BluetoothSocketStream(private val mmSocket: BluetoothSocket) : Thread() {
    private var inStream: InputStream = mmSocket.inputStream
    private var outStream: OutputStream = mmSocket.outputStream

    override fun run() {
        // Keep listening to the InputStream until an exception occurs
        LoggerFactory.logger.debug("ConnectedThread running")
        while (true) {
            try {
                // Read from the InputStream
                var bytes = inStream.available()
                SystemClock.sleep(100) //pause and wait for rest of data. Adjust this depending on your sending speed.
                if (bytes != 0) {
                    val buffer = ByteArray(1024)
                    bytes = inStream.available() // how many bytes are ready to be read?
                    bytes = inStream.read(buffer, 0, bytes) // record how many bytes we actually read

                    val str = String(buffer, 0, bytes)
                    LoggerFactory.logger.debug("received $bytes bytes: $str")
                }
            } catch (e: IOException) {
                LoggerFactory.logger.error(e)
                break
            }
        }
    }

    fun write(input: String) {
        try {
            outStream.write(input.toByteArray())
        } catch (e: IOException) {
            LoggerFactory.logger.error(e)
        }
    }

    fun close() {
        try {
            mmSocket.close()
        } catch (e: IOException) {
            LoggerFactory.logger.error(e)
        }
    }
}