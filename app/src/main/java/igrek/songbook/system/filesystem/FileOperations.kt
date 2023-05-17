package igrek.songbook.system.filesystem

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

fun saveInputStreamToFile(inputStream: InputStream, file: File) {
    val outputStream = FileOutputStream(file)
    outputStream.use {
        var read: Int
        val buffer = ByteArray(1024)

        while (true) {
            read = inputStream.read(buffer)
            if (read == -1)
                break
            outputStream.write(buffer, 0, read)
        }
    }
}

fun copyFile(source: File?, dest: File?) {
    var inputStream: InputStream? = null
    var outputStream: OutputStream? = null
    try {
        inputStream = FileInputStream(source)
        outputStream = FileOutputStream(dest)
        val buffer = ByteArray(1024)
        var length: Int
        while (inputStream.read(buffer).also { length = it } > 0) {
            outputStream.write(buffer, 0, length)
        }
    } finally {
        inputStream?.close()
        outputStream?.close()
    }
}
