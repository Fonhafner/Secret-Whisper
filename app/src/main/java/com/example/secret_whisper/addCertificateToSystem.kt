package com.example.secret_whisper

import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import java.security.KeyStore
import java.security.cert.CertificateFactory
import javax.net.ssl.SSLSocketFactory

fun getCustomSSLSocketFactory(): SSLSocketFactory {
    try {
        // Получение доступа к хранилищу доверенных корневых сертификатов устройства
        val keyStore = KeyStore.getInstance("AndroidCAStore")
        keyStore.load(null, null)

        // Получение алиаса вашего сертификата
        val alias = "user:887d7acf.0"

        // Получение сертификата из хранилища по алиасу
        val certificate = keyStore.getCertificate(alias)

        // Создание TrustManagerFactory и инициализация его с вашим KeyStore
        val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        trustManagerFactory.init(keyStore)

        // Создание SSLContext и настройка его с TrustManagerFactory
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, trustManagerFactory.trustManagers, null)

        // Получение SSLSocketFactory из SSLContext и возврат его
        return sslContext.socketFactory
    } catch (e: Exception) {
        throw RuntimeException("Failed to create SSLSocketFactory", e)
    }
}


