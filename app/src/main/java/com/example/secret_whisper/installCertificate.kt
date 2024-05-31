package com.example.secret_whisper


import android.content.Context
import android.content.Intent
import android.security.KeyChain
import java.io.FileOutputStream
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate

// Функция для экспорта и установки сертификата в хранилище доверенных сертификатов на устройстве
fun installCertificate(context: Context) {
    try {
        // Получение сертификата из ресурсов
        val inputStream = context.resources.openRawResource(R.raw.sertifi)

        // Конвертация сертификата в X509Certificate
        val certificateFactory = CertificateFactory.getInstance("X.509")
        val certificate = certificateFactory.generateCertificate(inputStream) as X509Certificate

        // Создание объекта KeyStore и добавление сертификата
        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
        keyStore.load(null, null)
        keyStore.setCertificateEntry("my_certificate", certificate)

        // Сохранение KeyStore в файл
        val fileName = "my_keystore.bks"
        val fos = context.openFileOutput(fileName, Context.MODE_PRIVATE)
        keyStore.store(fos, "password".toCharArray())
        fos.close()

        // Получение абсолютного пути к файлу KeyStore
        val file = context.getFileStreamPath(fileName)
        val absolutePath = file.absolutePath

        // Создание интента для установки сертификата
        val intent = KeyChain.createInstallIntent().apply {
            putExtra(KeyChain.EXTRA_CERTIFICATE, absolutePath)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}