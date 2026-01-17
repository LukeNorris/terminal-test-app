package com.example.terminal_test_app.data.remote.crypto

import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import org.json.JSONObject

/**
 * Implements Adyen NEXO local-comm encryption scheme:
 * - Derive key material with PBKDF2-HMAC-SHA1(passphrase, "AdyenNexoV1Salt", 4000, 80 bytes)
 *   - hmacKey:   bytes[0..31]
 *   - cipherKey: bytes[32..63]
 *   - iv:        bytes[64..79]
 * - Nonce (16 bytes) per message
 * - realIv = iv XOR nonce
 * - AES-256-CBC-PKCS5Padding encrypt full original message bytes with cipherKey + realIv
 * - HMAC-SHA256 over full original message bytes with hmacKey
 * - Wrap result in { SaleToPOIRequest|SaleToPOIResponse: { MessageHeader, NexoBlob, SecurityTrailer } }
 */
class NexoCrypto(
    private val keyIdentifier: String,
    private val keyVersion: Long,
    passphrase: CharArray
) {

    data class DerivedKeys(
        val hmacKey: ByteArray,   // 32 bytes
        val cipherKey: ByteArray, // 32 bytes
        val iv: ByteArray         // 16 bytes
    )

    private val derivedKeys: DerivedKeys = deriveKeyMaterial(passphrase)

    companion object {
        private const val SALT = "AdyenNexoV1Salt"
        private const val ITERATIONS = 4000
        private const val OUT_LEN_BYTES = 80

        private const val HMAC_KEY_LEN = 32
        private const val CIPHER_KEY_LEN = 32
        private const val IV_LEN = 16

        private fun b64(bytes: ByteArray): String =
            Base64.encodeToString(bytes, Base64.NO_WRAP)

        private fun b64decode(str: String): ByteArray =
            Base64.decode(str, Base64.DEFAULT)

        /**
         * PBKDF2WithHmacSHA1, 80 bytes output.
         */
        fun deriveKeyMaterial(passphrase: CharArray): DerivedKeys {
            val spec = PBEKeySpec(
                passphrase,
                SALT.toByteArray(StandardCharsets.UTF_8),
                ITERATIONS,
                OUT_LEN_BYTES * 8
            )
            val skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
            val bytes = skf.generateSecret(spec).encoded

            val hmacKey = bytes.copyOfRange(0, HMAC_KEY_LEN)
            val cipherKey = bytes.copyOfRange(HMAC_KEY_LEN, HMAC_KEY_LEN + CIPHER_KEY_LEN)
            val iv = bytes.copyOfRange(HMAC_KEY_LEN + CIPHER_KEY_LEN, OUT_LEN_BYTES)

            require(hmacKey.size == HMAC_KEY_LEN)
            require(cipherKey.size == CIPHER_KEY_LEN)
            require(iv.size == IV_LEN)

            return DerivedKeys(hmacKey = hmacKey, cipherKey = cipherKey, iv = iv)
        }

        private fun xor16(a: ByteArray, b: ByteArray): ByteArray {
            require(a.size == IV_LEN) { "Expected 16 bytes for a" }
            require(b.size == IV_LEN) { "Expected 16 bytes for b" }
            return ByteArray(IV_LEN) { i -> (a[i].toInt() xor b[i].toInt()).toByte() }
        }

        private fun timingSafeEquals(a: ByteArray, b: ByteArray): Boolean {
            if (a.size != b.size) return false
            var diff = 0
            for (i in a.indices) {
                diff = diff or (a[i].toInt() xor b[i].toInt())
            }
            return diff == 0
        }
    }

    /**
     * Encrypt + HMAC and wrap into NexoBlob/SecurityTrailer structure.
     *
     * @param plainJson Full original JSON string (e.g. {"SaleToPOIRequest":{...}} )
     * @return secured JSON string (same outer body key, header copied, with NexoBlob + SecurityTrailer)
     */
    fun encryptAndWrap(plainJson: String): String {
        val plainBytes = plainJson.toByteArray(StandardCharsets.UTF_8)

        // Parse JSON to find body key and MessageHeader
        val jsonIn = JSONObject(plainJson)
        val bodyKey = when {
            jsonIn.has("SaleToPOIRequest") -> "SaleToPOIRequest"
            jsonIn.has("SaleToPOIResponse") -> "SaleToPOIResponse"
            else -> throw IllegalArgumentException("Expected SaleToPOIRequest or SaleToPOIResponse root key")
        }
        val bodyObj = jsonIn.getJSONObject(bodyKey)
        val header = bodyObj.getJSONObject("MessageHeader")

        // Nonce (iv modifier)
        val nonce = ByteArray(IV_LEN).also { SecureRandom().nextBytes(it) }
        val realIv = xor16(derivedKeys.iv, nonce)

        // AES-256-CBC
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val aesKey = SecretKeySpec(derivedKeys.cipherKey, "AES")
        cipher.init(Cipher.ENCRYPT_MODE, aesKey, IvParameterSpec(realIv))
        val ciphertext = cipher.doFinal(plainBytes)

        // HMAC-SHA256 over *plaintext* bytes
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(derivedKeys.hmacKey, "HmacSHA256"))
        val hmac = mac.doFinal(plainBytes)

        // Security trailer
        val trailer = JSONObject()
            .put("KeyVersion", keyVersion)
            .put("KeyIdentifier", keyIdentifier)
            .put("Hmac", b64(hmac))
            .put("Nonce", b64(nonce))
            .put("AdyenCryptoVersion", 1)

        // Compose secured body (header copied)
        val securedBody = JSONObject()
            .put("MessageHeader", header)
            .put("NexoBlob", b64(ciphertext))
            .put("SecurityTrailer", trailer)

        return JSONObject().put(bodyKey, securedBody).toString()
    }

    /**
     * Decrypt + validate HMAC + validate that inner MessageHeader matches outer MessageHeader.
     *
     * @param securedJson secured message as produced by encryptAndWrap
     * @param passphraseLookup callback to fetch passphrase given KeyIdentifier + KeyVersion from trailer
     * @return decrypted original JSON string if valid, otherwise throws.
     */
    fun decryptAndValidate(
        securedJson: String,
        passphraseLookup: (keyIdentifier: String, keyVersion: Long) -> CharArray
    ): String {
        val jsonIn = JSONObject(securedJson)
        val bodyKey = when {
            jsonIn.has("SaleToPOIRequest") -> "SaleToPOIRequest"
            jsonIn.has("SaleToPOIResponse") -> "SaleToPOIResponse"
            else -> throw IllegalArgumentException("Expected SaleToPOIRequest or SaleToPOIResponse root key")
        }
        val securedBody = jsonIn.getJSONObject(bodyKey)

        val outerHeader = securedBody.optJSONObject("MessageHeader")
            ?: throw IllegalArgumentException("MessageHeader not found")
        val blobB64 = securedBody.optString("NexoBlob", null)
            ?: throw IllegalArgumentException("NexoBlob not found")
        val trailer = securedBody.optJSONObject("SecurityTrailer")
            ?: throw IllegalArgumentException("SecurityTrailer not found")

        val cryptoVersion = trailer.optInt("AdyenCryptoVersion", -1)
        if (cryptoVersion != 1) throw IllegalArgumentException("Unsupported AdyenCryptoVersion: $cryptoVersion")

        val keyId = trailer.optString("KeyIdentifier", null)
            ?: throw IllegalArgumentException("KeyIdentifier not found")
        val keyVer = trailer.optLong("KeyVersion", Long.MIN_VALUE)
        if (keyVer == Long.MIN_VALUE) throw IllegalArgumentException("KeyVersion not found")

        val nonce = b64decode(trailer.optString("Nonce", ""))
        val receivedHmac = b64decode(trailer.optString("Hmac", ""))

        if (nonce.size != IV_LEN) throw IllegalArgumentException("Nonce must be 16 bytes")

        // Derive keys for the message using lookup
        val passphrase = passphraseLookup(keyId, keyVer)
        val dk = deriveKeyMaterial(passphrase)

        val ciphertext = b64decode(blobB64)
        val realIv = xor16(dk.iv, nonce)

        // Decrypt
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(dk.cipherKey, "AES"), IvParameterSpec(realIv))
        val plainBytes = cipher.doFinal(ciphertext)
        val plainJson = String(plainBytes, StandardCharsets.UTF_8)

        // Validate HMAC over plaintext
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(dk.hmacKey, "HmacSHA256"))
        val computedHmac = mac.doFinal(plainBytes)

        if (!timingSafeEquals(receivedHmac, computedHmac)) {
            throw IllegalArgumentException("HMAC validation failed")
        }

        // Validate inner header equals outer header
        val decryptedObj = JSONObject(plainJson)
        val innerBody = decryptedObj.getJSONObject(bodyKey)
        val innerHeader = innerBody.optJSONObject("MessageHeader")
            ?: throw IllegalArgumentException("Inner MessageHeader missing")

        if (!jsonObjectsDeepEqual(innerHeader, outerHeader)) {
            throw IllegalArgumentException("MessageHeader mismatch (inner != outer)")
        }

        return plainJson
    }

    private fun jsonObjectsDeepEqual(a: JSONObject, b: JSONObject): Boolean {
        // Simple deep equality for JSONObject (order-independent)
        if (a.length() != b.length()) return false
        val keys = a.keys()
        while (keys.hasNext()) {
            val k = keys.next()
            if (!b.has(k)) return false
            val va = a.get(k)
            val vb = b.get(k)
            if (va is JSONObject && vb is JSONObject) {
                if (!jsonObjectsDeepEqual(va, vb)) return false
            } else {
                if (va != vb) return false
            }
        }
        return true
    }
}
