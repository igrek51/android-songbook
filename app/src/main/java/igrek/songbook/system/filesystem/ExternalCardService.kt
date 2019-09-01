package igrek.songbook.system.filesystem


import android.app.Activity
import android.os.Build
import android.os.Environment
import igrek.songbook.info.logger.LoggerFactory
import java.io.File
import java.util.*

/**
 * service to find external sd card location (it's not so obvious)
 */
class ExternalCardService {

    private val logger = LoggerFactory.logger
    private val externalSDPath: String?

    private val externalStorageDirectory: String
        get() {
            logger.warn("getting Environment.getExternalStorageDirectory() -  it's probably not what it's named for")
            return Environment.getExternalStorageDirectory().absolutePath
        }

    private val isSamsung: Boolean
        get() = Build.DEVICE.contains("samsung") || Build.MANUFACTURER.contains("samsung")

    private val externalMount: String?
        get() {
            val externalMounts = externalMounts
            if (externalMounts.size > 1) {
                logger.warn("multiple external mounts found, getting the first one")
            }
            return if (externalMounts.isNotEmpty()) externalMounts.iterator().next() else null
        }

    private// parse output
    // lg workaround - brilliant
    val externalMounts: HashSet<String>
        get() {
            val out = HashSet<String>()
            val reg = "(?i).*vold.*(vfat|ntfs|exfat|fat32|ext3|ext4).*rw.*"
            val s = StringBuilder()
            try {
                val process = ProcessBuilder().command("mount")
                        .redirectErrorStream(true)
                        .start()
                process.waitFor()
                val `is` = process.inputStream
                val buffer = ByteArray(1024)
                while (`is`.read(buffer) != -1) {
                    s.append(String(buffer))
                }
                `is`.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val lines = s.toString().split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            for (line in lines) {
                if (!line.toLowerCase(Locale.US).contains("asec")) {
                    if (line.matches(reg.toRegex())) {
                        val parts = line.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                        for (part in parts) {
                            if (part.startsWith("/"))
                                if (!part.toLowerCase(Locale.US).contains("vold")) {
                                    var partr = part
                                    if (Build.MANUFACTURER.contains("LGE"))
                                        partr = part.replace("^/mnt/media_rw".toRegex(), "/storage")
                                    out.add(partr)
                                }
                        }
                    }
                }
            }
            return out
        }

    init {
        externalSDPath = findExternalSDPath()
        logger.debug("External SD Card path detected: " + externalSDPath!!)
    }

    private fun findExternalSDPath(): String? {
        return FirstRuleChecker<String>()
                .addRule({ this.isSamsung }, { checkDirExists("/storage/extSdCard") })
                .addRule { this.externalMount }
                .addRule { checkDirExists("/storage/extSdCard") }
                .addRule { checkDirExists("/storage/external_sd") }
                .addRule { checkDirExists("/storage/ext_sd") }
                .addRule { checkDirExists("/storage/external") }
                .addRule { this.externalStorageDirectory }
                .find()
    }

    private fun checkDirExists(path: String): String? {
        val f = File(path)
        return if (f.exists() && f.isDirectory) path else null
    }

}
