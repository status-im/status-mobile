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
  public OkHttpClient createNewNetworkModuleClient() {
    String certPem = StatusPackage.getImageTLSCert();
    X509Certificate cert;

    try {
      CertificateFactory cf = CertificateFactory.getInstance("X.509");
      cert = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(certPem.getBytes()));
    } catch(Exception e) {
      Log.e("StatusOkHttpClientFactory", "Could not parse certificate");
      cert = null;
    }

    HandshakeCertificates clientCertificates = new HandshakeCertificates.Builder()
      .addPlatformTrustedCertificates()
      .addTrustedCertificate(cert)
      .build();

    return OkHttpClientProvider.createClientBuilder()
      .sslSocketFactory(clientCertificates.sslSocketFactory(), clientCertificates.trustManager())
      .build();
  }
}
