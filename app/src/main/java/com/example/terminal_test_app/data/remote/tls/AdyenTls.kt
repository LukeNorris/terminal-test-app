/*package com.example.terminal_test_app.data.remote.tls

import android.content.Context
import com.example.terminal_test_app.R
import com.example.terminal_test_app.data.settings.TerminalEnvironment
import java.security.KeyStore
import java.security.SecureRandom
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.*

// data/remote/tls/AdyenTls.kt
object AdyenTls {
    fun createTrustManager(context: Context, environment: TerminalEnvironment): X509TrustManager {
        val certRes = when (environment) {
            TerminalEnvironment.TEST -> R.raw.adyen_terminalfleet_test
            TerminalEnvironment.LIVE -> R.raw.adyen_terminalfleet_live
        }

        return try {
            val cf = CertificateFactory.getInstance("X.509")
            // Open as a raw byte stream
            val caCert = context.resources.openRawResource(certRes).use { inputStream ->
                cf.generateCertificate(inputStream) as X509Certificate
            }

            val keyStore = KeyStore.getInstance(KeyStore.getDefaultType()).apply {
                load(null, null)
                setCertificateEntry("adyen_root", caCert)
            }

            val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm()).apply {
                init(keyStore)
            }

            tmf.trustManagers.filterIsInstance<X509TrustManager>().single()
        } catch (e: Exception) {
            android.util.Log.e("AdyenTls", "CRITICAL: Certificate Loading Failed!", e)
            throw e
        }
    }

    fun createSslContext(trustManager: X509TrustManager): SSLContext =
        SSLContext.getInstance("TLSv1.2").apply {
            init(null, arrayOf(trustManager), java.security.SecureRandom())
        }
}*/

package com.example.terminal_test_app.data.remote.tls

import android.annotation.SuppressLint
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.*

object AdyenTls {

    @SuppressLint("CustomX509TrustManager")
    fun createUnsafeTrustManager(): X509TrustManager {
        return object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        }
    }

    fun createSslContext(trustManager: X509TrustManager): SSLContext =
        SSLContext.getInstance("TLS").apply {
            init(null, arrayOf(trustManager), SecureRandom())
        }
}