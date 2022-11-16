package igrek.songbook.secret

import java.security.MessageDigest

class ShaHasher {

    fun hash(input: String): String {
        var result = singleHash(input + salt)
        repeat(iterations) {
            result = singleHash(result)
        }
        return result
    }

    fun singleHash(input: String): String {
        val bytes = input.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

    companion object {
        private const val salt = "hush, hush!"
        private const val iterations = 10
    }
}