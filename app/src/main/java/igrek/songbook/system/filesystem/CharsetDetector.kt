package igrek.songbook.system.filesystem

import igrek.songbook.info.logger.LoggerFactory

class CharsetDetector {

    private val cp1250PLBytes = byteArrayOf(0xB9.toByte(), 0xBF.toByte(), 0x9C.toByte(), 0x9F.toByte(), 0xEA.toByte(), 0xE6.toByte(), 0xF1.toByte(), 0xF3.toByte(), 0xB3.toByte(), 0xA5.toByte(), 0xAF.toByte(), 0x8C.toByte(), 0x8F.toByte(), 0xCA.toByte(), 0xC6.toByte(), 0xD1.toByte(), 0xD3.toByte(), 0xA3.toByte())
    private val utf8PLPrefixBytes = byteArrayOf(0xC3.toByte(), 0xC4.toByte(), 0xC5.toByte())
    private val CHARSET_UTF8 = "UTF-8"
    private val CHARSET_CP1250 = "Cp1250"
    private val logger = LoggerFactory.logger

    fun detect(bytes: ByteArray): String {
        // if file contains special characters from utf8
        if (containsBytes(bytes, utf8PLPrefixBytes)) {
            logger.info("Encoding detected: $CHARSET_UTF8")
            return CHARSET_UTF8
        }

        // if file contains polish letters from CP1250
        if (containsBytes(bytes, cp1250PLBytes)) {
            logger.info("Encoding detected: $CHARSET_CP1250")
            return CHARSET_CP1250
        }

        logger.info("Default encoding: $CHARSET_UTF8")
        return CHARSET_UTF8
    }

    fun repair(`in`: ByteArray, charset: String): ByteArray {
        if (charset == CHARSET_UTF8) {
            for (i in `in`.indices) {
                //krzaki zamiast apostrofu
                if (`in`[i] == 0x92.toByte()) {
                    `in`[i] = '\''.toByte()
                }
            }
        }
        return `in`
    }

    private fun byte2hex(b: Byte): String {
        return String.format("%02X ", b)
    }

    private fun containsByte(bytes: ByteArray, b: Byte): Boolean {
        for (b1 in bytes) {
            if (b1 == b)
                return true
        }
        return false
    }

    private fun containsBytes(bytes1: ByteArray, bytes2: ByteArray): Boolean {
        for (b1 in bytes1) {
            if (containsByte(bytes2, b1)) {
                return true
            }
        }
        return false
    }
}
