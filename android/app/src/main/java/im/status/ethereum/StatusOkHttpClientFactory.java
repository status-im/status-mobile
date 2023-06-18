package im.status.ethereum;

import android.util.Log;

import com.facebook.react.modules.network.OkHttpClientFactory;
import com.facebook.react.modules.network.OkHttpClientProvider;

import okhttp3.OkHttpClient;
import okhttp3.Interceptor;
import okhttp3.tls.HandshakeCertificates;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.RuntimeException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import im.status.ethereum.module.StatusPackage;

class StatusOkHttpClientFactory implements OkHttpClientFactory {

  private static final String TAG = "StatusOkHttpClientFactory";

  public OkHttpClient createNewNetworkModuleClient() {
      X509Certificate cert = null;
      HandshakeCertificates clientCertificates;
      String certPem = "";

    // Get TLS PEM certificate from status-go
    try {
        // induce half second sleep because sometimes a cert is not immediately available
        // TODO : remove sleep if App no longer crashes on Android 10 devices with
        // java.lang.RuntimeException: Could not invoke WebSocketModule.connect
        Thread.sleep(500);
        certPem = StatusPackage.getImageTLSCert();
    } catch(Exception e) {
        Log.e(TAG, "Could not getImageTLSCert",e);
    }
    // Convert PEM certificate string to X509Certificate object
    try {
      // induce half second sleep because sometimes a cert is not immediately available
      // TODO : remove sleep if App no longer crashes on Android 10 devices
      // java.lang.RuntimeException: Could not invoke WebSocketModule.connect
      Thread.sleep(500);
      CertificateFactory cf = CertificateFactory.getInstance("X.509");
      cert = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(certPem.getBytes()));
    } catch(Exception e) {
      Log.e(TAG, "Could not parse certificate",e);
    }
    // Create HandshakeCertificates object with our certificate
    try {
      // induce half second sleep because sometimes a cert is not immediately available
      // TODO : remove sleep if App no longer crashes on Android 10 devices
      // java.lang.RuntimeException: Could not invoke WebSocketModule.connect
      Thread.sleep(500);
      clientCertificates = new HandshakeCertificates.Builder()
        .addPlatformTrustedCertificates()
        .addTrustedCertificate(cert)
        .build();
    } catch(Exception e) {
      Log.e(TAG, "Could not build HandshakeCertificates", e);
      return null;
    }

    // Create OkHttpClient with custom SSL socket factory and trust manager
    try {
       return OkHttpClientProvider.createClientBuilder()
        .sslSocketFactory(clientCertificates.sslSocketFactory(), clientCertificates.trustManager())
        .build();
    } catch(Exception e) {
      Log.e(TAG, "Could not create OkHttpClient", e);
      return null;
    }
  }
}
