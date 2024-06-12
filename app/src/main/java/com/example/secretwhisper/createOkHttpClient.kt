


import android.content.Context
import okhttp3.OkHttpClient
import java.security.KeyStore
import java.security.SecureRandom
import java.security.cert.CertificateFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

fun createOkHttpClient(context: Context, certificateRawResource: Int): OkHttpClient {
    val certificateFactory = CertificateFactory.getInstance("X.509")
    val certificate = context.resources.openRawResource(certificateRawResource).use {
        certificateFactory.generateCertificate(it)
    }

    val keyStore = KeyStore.getInstance(KeyStore.getDefaultType()).apply {
        load(null)
        setCertificateEntry("ca", certificate)
    }

    val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm()).apply {
        init(keyStore)
    }

    val sslContext = SSLContext.getInstance("TLS").apply {
        init(null, trustManagerFactory.trustManagers, SecureRandom())
    }

    val trustManager = trustManagerFactory.trustManagers[0] as X509TrustManager

    return OkHttpClient.Builder()
        .sslSocketFactory(sslContext.socketFactory, trustManager)
        .build()
}
