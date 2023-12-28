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
    }

    override fun createNewNetworkModuleClient(): OkHttpClient? {
        var cert: X509Certificate? = null
        lateinit var clientCertificates: HandshakeCertificates
        var certPem = ""
        // Get TLS PEM certificate from status-go
        try {
            // induce half second sleep because sometimes a cert is not immediately available
            // TODO : remove sleep if App no longer crashes on Android 10 devices with
            // java.lang.RuntimeException: Could not invoke WebSocketModule.connect
            Thread.sleep(500)
            certPem = getCertificatePem()
        } catch (e: Exception) {
            Log.e(TAG, "Could not getImageTLSCert", e)
        }

        if (certPem.isEmpty()) {
            Log.e(TAG, "Certificate is empty, cannot create OkHttpClient without a valid certificate")
            return null
        }

        // Convert PEM certificate string to X509Certificate object
        try {
            // induce half second sleep because sometimes a cert is not immediately available
            // TODO : remove sleep if App no longer crashes on Android 10 devices
            // java.lang.RuntimeException: Could not invoke WebSocketModule.connect
            Thread.sleep(500)
            val cf = CertificateFactory.getInstance("X.509")
            val tempCert = cf.generateCertificate(ByteArrayInputStream(certPem.toByteArray())) as X509Certificate?
            if (tempCert != null) {
                cert = tempCert
            } else {
                Log.e(TAG, "Certificate could not be parsed as non-null")
                return null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Could not parse certificate", e)
        }
        // Create HandshakeCertificates object with our certificate
        try {
            // induce half second sleep because sometimes a cert is not immediately available
            // TODO : remove sleep if App no longer crashes on Android 10 devices
            // java.lang.RuntimeException: Could not invoke WebSocketModule.connect
            Thread.sleep(500)
            if (cert != null) {
                clientCertificates = HandshakeCertificates.Builder()
                    .addPlatformTrustedCertificates()
                    .addTrustedCertificate(cert)
                    .build()
            } else {
                Log.e(TAG, "Certificate is null, cannot build HandshakeCertificates")
                return null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Could not build HandshakeCertificates", e)
            return null
        }

        // Create OkHttpClient with custom SSL socket factory and trust manager
        try {
            val clientCertificatesBuilder = OkHttpClientProvider.createClientBuilder()
            if (clientCertificates.sslSocketFactory() != null && clientCertificates.trustManager != null) {
                return clientCertificatesBuilder
                       .sslSocketFactory(clientCertificates.sslSocketFactory(), clientCertificates.trustManager)
                       .build()
                } else {
                    Log.e(TAG, "SSL Socket Factory or Trust Manager is null")
                    return null
                }
        } catch (e: Exception) {
            Log.e(TAG, "Could not create OkHttpClient", e)
            return null
        }
    }

    private fun getCertificatePem(): String {
        return try {
            val certPem = StatusPackage.getImageTLSCert()
            if (certPem == null || certPem.trim().isEmpty()) {
                Log.e(TAG, "Certificate PEM string is null or empty")
                ""
            } else {
                certPem
            }
        } catch (e: Exception) {
            Log.e(TAG, "Could not getImageTLSCert", e)
            ""
        }
    }
}
