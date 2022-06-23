package igrek.songbook.billing

import android.text.TextUtils
import android.util.Base64
import igrek.songbook.info.logger.Logger
import igrek.songbook.info.logger.LoggerFactory
import java.io.IOException
import java.lang.IllegalArgumentException
import java.lang.RuntimeException
import java.security.*
import java.security.spec.InvalidKeySpecException
import java.security.spec.X509EncodedKeySpec
import kotlin.Throws

internal object Security {

    private const val KEY_FACTORY_ALGORITHM = "RSA"
    private const val SIGNATURE_ALGORITHM = "SHA1withRSA"

    private const val BASE_64_ENCODED_PUBLIC_KEY = BILLING_PUBLIC_KEY

    private val logger: Logger = LoggerFactory.logger

    fun verifyPurchase(signedData: String, signature: String?): Boolean {
        if (TextUtils.isEmpty(signedData) || TextUtils.isEmpty(BASE_64_ENCODED_PUBLIC_KEY)
                || TextUtils.isEmpty(signature)) {
            logger.warn("Purchase verification failed: missing data.")
            return false
        }
        return try {
            val key = generatePublicKey(BASE_64_ENCODED_PUBLIC_KEY)
            verify(key, signedData, signature)
        } catch (e: IOException) {
            logger.error("Error generating PublicKey from encoded key: " + e.message)
            false
        }
    }

    @Throws(IOException::class)
    private fun generatePublicKey(encodedPublicKey: String): PublicKey {
        return try {
            val decodedKey = Base64.decode(encodedPublicKey, Base64.DEFAULT)
            val keyFactory = KeyFactory.getInstance(KEY_FACTORY_ALGORITHM)
            keyFactory.generatePublic(X509EncodedKeySpec(decodedKey))
        } catch (e: NoSuchAlgorithmException) {
            // "RSA" is guaranteed to be available.
            throw RuntimeException(e)
        } catch (e: InvalidKeySpecException) {
            val msg = "Invalid key specification: $e"
            logger.warn(msg)
            throw IOException(msg)
        }
    }

    private fun verify(publicKey: PublicKey, signedData: String, signature: String?): Boolean {
        val signatureBytes: ByteArray = try {
            Base64.decode(signature, Base64.DEFAULT)
        } catch (e: IllegalArgumentException) {
            logger.warn("Base64 decoding failed.")
            return false
        }
        try {
            val signatureAlgorithm = Signature.getInstance(SIGNATURE_ALGORITHM)
            signatureAlgorithm.initVerify(publicKey)
            signatureAlgorithm.update(signedData.toByteArray())
            if (!signatureAlgorithm.verify(signatureBytes)) {
                logger.warn("Signature verification failed...")
                return false
            }
            return true
        } catch (e: NoSuchAlgorithmException) {
            // "RSA" is guaranteed to be available.
            throw RuntimeException(e)
        } catch (e: InvalidKeyException) {
            logger.error("Invalid key specification.")
        } catch (e: SignatureException) {
            logger.error("Signature exception.")
        }
        return false
    }
}
