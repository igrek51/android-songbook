package igrek.songbook.system.filesystem

import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

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