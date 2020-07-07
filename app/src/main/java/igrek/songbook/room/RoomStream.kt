package igrek.songbook.room

import android.bluetooth.BluetoothSocket
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.info.logger.LoggerFactory.logger
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class RoomStream(
        private val remoteSocket: BluetoothSocket,
) : Thread() {
    private var inStream: InputStream = remoteSocket.inputStream
    private var outStream: OutputStream = remoteSocket.outputStream
    private var inBuffer = StringBuffer()

    companion object {
        const val GTR_VERSION = 1
    }

    override fun run() {
        // Keep listening to the InputStream until an exception occurs
        logger.debug("ConnectedThread running")
        while (remoteSocket.isConnected) {
            try {
                var bytes = inStream.available()
                runBlocking {
                    delay(100L) //pause and wait for rest of data.
                }
                if (bytes != 0) {
                    val buffer = ByteArray(1024)
                    bytes = inStream.available() // how many bytes are ready to be read?
                    bytes = inStream.read(buffer, 0, bytes) // record how many bytes we actually read

                    val str = String(buffer, 0, bytes)
                    inBuffer.append(str)
                    LoggerFactory.logger.debug("received $bytes bytes: $str")

                    findCompleteMessage()
                }
            } catch (e: IOException) {
                logger.error(e)
                break
            }
        }
    }

    fun write(input: String) {
        try {
            outStream.write(input.toByteArray())
            outStream.write(0)
            outStream.flush()
        } catch (e: IOException) {
            logger.error(e)
        }
    }

    fun close() {
        try {
            remoteSocket.close()
        } catch (e: IOException) {
            logger.error(e)
        }
    }

    fun findCompleteMessage() {
        val firstZero = inBuffer.indexOf(0.toChar())
        if (firstZero == -1)
            return

        val message = inBuffer.take(firstZero)
        inBuffer.delete(0, firstZero + 1)

        processMessage(message.toString())

        findCompleteMessage()
    }

    private fun processMessage(message: String) {
        logger.debug("processed message: $message (${message.length})")
    }
}