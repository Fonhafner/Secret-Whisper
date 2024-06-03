package com.example.secret_whisper

import android.content.Context
import com.example.secretwhisper.R
import okhttp3.OkHttpClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Request
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.security.KeyStore
import java.security.cert.CertificateFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

fun initializeOkHttpClient(context: Context): OkHttpClient {
    // Load certificate from raw resources
    val certificateInputStream: InputStream = context.resources.openRawResource(R.raw.sertifi)
    val certificateFactory = CertificateFactory.getInstance("X.509")
    val certificate = certificateFactory.generateCertificate(certificateInputStream)

    // Create KeyStore and add the certificate to it
    val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
    keyStore.load(null, null)
    keyStore.setCertificateEntry("certificate", certificate)

    // Create TrustManager using TrustManagerFactory
    val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
    trustManagerFactory.init(keyStore)
    val trustManagers = trustManagerFactory.trustManagers

    // Get X509TrustManager from the array of TrustManagers
    val trustManager = trustManagers[0] as X509TrustManager

    // Create SSLContext and configure it using X509TrustManager
    val sslContext = SSLContext.getInstance("TLS")
    sslContext.init(null, arrayOf(trustManager), null)

    // Create OkHttpClient with SSLContext and TrustManager settings
    return OkHttpClient.Builder()
        .sslSocketFactory(sslContext.socketFactory, trustManager)
        .build()
}
