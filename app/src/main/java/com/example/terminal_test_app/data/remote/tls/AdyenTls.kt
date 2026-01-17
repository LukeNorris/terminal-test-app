package com.example.terminal_test_app.data.remote.tls

import android.content.Context
import com.example.terminal_test_app.R
import com.example.terminal_test_app.data.settings.TerminalEnvironment
import java.security.KeyStore
import java.security.SecureRandom
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.*

object AdyenTls {

    fun createTrustManager(
        context: Context,
        environment: TerminalEnvironment
    ): X509TrustManager {

        val certRes = when (environment) {
            TerminalEnvironment.TEST -> R.raw.adyen_terminalfleet_test
            TerminalEnvironment.LIVE -> R.raw.adyen_terminalfleet_live
        }

        val caCert = context.resources.openRawResource(certRes).use {
            CertificateFactory.getInstance("X.509")
                .generateCertificate(it) as X509Certificate
        }

        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType()).apply {
            load(null, null)
            setCertificateEntry("adyen_root", caCert)
        }

        val tmf = TrustManagerFactory.getInstance(
            TrustManagerFactory.getDefaultAlgorithm()
        ).apply {
            init(keyStore)
        }

        return tmf.trustManagers
            .filterIsInstance<X509TrustManager>()
            .single()
    }

    fun createSslContext(trustManager: X509TrustManager): SSLContext =
        SSLContext.getInstance("TLS").apply {
            init(null, arrayOf(trustManager), SecureRandom())
        }
}
