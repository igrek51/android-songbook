package igrek.songbook.room

import android.bluetooth.BluetoothSocket
import igrek.songbook.info.logger.LoggerFactory.logger
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class PeerStream(
        private val remoteSocket: BluetoothSocket,
        val receivedMsgCh: Channel<String>,
) : Thread() {
    private var inStream: InputStream = remoteSocket.inputStream
    private var outStream: OutputStream = remoteSocket.outputStream
    private var inBuffer = StringBuffer()
    private val writeMutex = Mutex()

    override fun run() {
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

                    findCompleteMessage()
                }
            } catch (e: IOException) {
                logger.error(e)
                break
            }
        }
        close()
    }

    fun write(input: String) {
        if (!isAlive)
            throw RuntimeException("peer disconnected")
        try {
            runBlocking {
                writeMutex.withLock {
                    outStream.write(input.toByteArray())
                    outStream.write(0)
                    outStream.flush()
                }
            }
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

    private fun findCompleteMessage() {
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
        GlobalScope.launch {
            receivedMsgCh.send(message)
        }
    }

    fun remoteAddress(): String {
        return remoteSocket.remoteDevice.address
    }

    fun remoteName(): String {
        return remoteSocket.remoteDevice.name
    }

}