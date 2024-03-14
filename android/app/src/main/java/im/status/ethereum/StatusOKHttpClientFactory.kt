package im.status.ethereum

import android.util.Log
import com.facebook.react.modules.network.OkHttpClientFactory
import com.facebook.react.modules.network.OkHttpClientProvider
import okhttp3.OkHttpClient
import okhttp3.tls.HandshakeCertificates
import java.io.ByteArrayInputStream
import java.lang.RuntimeException
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import im.status.ethereum.module.StatusPackage

class StatusOkHttpClientFactory : OkHttpClientFactory {

    companion object {
        private const val TAG = "StatusOkHttpClientFactory"
        private const val SLEEP_DURATION = 500L // milliseconds
    }

    override fun createNewNetworkModuleClient(): OkHttpClient? {
        val certPem = getCertificatePem().takeIf { it.isNotEmpty() }
            ?: return logAndReturnNull("Certificate is empty, cannot create OkHttpClient without a valid certificate")

        val cert = try {
            induceSleep()
            // Convert PEM certificate string to X509Certificate object
            CertificateFactory.getInstance("X.509")
                .generateCertificate(ByteArrayInputStream(certPem.toByteArray())) as? X509Certificate
                ?: return logAndReturnNull("Certificate could not be parsed as non-null")
        } catch (e: Exception) {
            return logAndReturnNull("Could not parse certificate", e)
        }

        val clientCertificates = try {
            induceSleep()
            // Create HandshakeCertificates object with our certificate
            HandshakeCertificates.Builder()
                .addPlatformTrustedCertificates()
                .addTrustedCertificate(cert)
                .build()
        } catch (e: Exception) {
            return logAndReturnNull("Could not build HandshakeCertificates", e)
        }

        return try {
            OkHttpClientProvider.createClientBuilder()
                .sslSocketFactory(clientCertificates.sslSocketFactory(), clientCertificates.trustManager)
                .build()
        } catch (e: Exception) {
            logAndReturnNull("Could not create OkHttpClient", e)
        }
    }

    private fun getCertificatePem(): String {
        return try {
            // Create OkHttpClient with custom SSL socket factory and trust manager
            StatusPackage.getImageTLSCert().takeIf { !it.isNullOrBlank() }
                ?: logAndReturnEmpty("Certificate PEM string is null or empty")
        } catch (e: Exception) {
            logAndReturnEmpty("Could not getImageTLSCert", e)
        }
    }

    private fun induceSleep() {
        try {
            // induce half second sleep because sometimes a cert is not immediately available
            // TODO : remove sleep if App no longer crashes on Android 10 devices with
            // java.lang.RuntimeException: Could not invoke WebSocketModule.connect
            Thread.sleep(SLEEP_DURATION)
        } catch (e: InterruptedException) {
            Log.e(TAG, "Sleep interrupted", e)
        }
    }

    private fun logAndReturnNull(message: String, e: Exception? = null): Nothing? {
        Log.e(TAG, message, e)
        return null
    }

    private fun logAndReturnEmpty(message: String, e: Exception? = null): String {
        Log.e(TAG, message, e)
        return ""
    }
}

