package com.example.secretwhisper


import android.content.Context
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.security.KeyStore
import java.security.PublicKey
import java.security.SecureRandom
import java.security.cert.CertificateFactory
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

object ChatManager {
    private const val CIPHER_TRANSFORMATION = "RSA/ECB/PKCS1Padding"
    private const val BASE_URL = "https://rtusecretwhisper.ru/api/"

    // Функция для загрузки открытого ключа другого пользователя
    private suspend fun getPublicKeyOfOtherUser(context: Context, otherUserName: String): PublicKey? {
        return withContext(Dispatchers.IO) {
            try {
                val client = createOkHttpClient(context, R.raw.sertifi)
                val json = JSONObject().apply {
                    put("username", otherUserName)
                }
                val requestBody = json.toString().toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url("https://rtusecretwhisper.ru/api/get_public_key")
                    .post(requestBody)
                    .build()
                val response = client.newCall(request).execute()
                response.use {
                    if (it.isSuccessful) {
                        val publicKeyString = it.body?.string()
                        if (!publicKeyString.isNullOrEmpty()) {
                            val publicKeyBytes = Base64.decode(publicKeyString, Base64.DEFAULT)
                            val keyFactory = java.security.KeyFactory.getInstance("RSA")
                            val keySpec = X509EncodedKeySpec(publicKeyBytes)
                            return@withContext keyFactory.generatePublic(keySpec)
                        }
                    }
                }
                null
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }






    // Функция для шифрования сообщения открытым ключом другого пользователя
    fun encryptMessageWithPublicKey(context: Context, otherUserName: String, message: String): String? {
        val publicKey = runBlocking { getPublicKeyOfOtherUser(context, otherUserName) } ?: return null
        val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        val encryptedBytes = cipher.doFinal(message.toByteArray())
        return Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
    }

    // Функция для расшифровки сообщения закрытым ключом пользователя
    fun decryptMessageWithPrivateKey(context: Context, encryptedMessage: String): String? {
        val privateKey = getPrivateKey(context) ?: return null
        val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, privateKey)
        val encryptedBytes = Base64.decode(encryptedMessage, Base64.DEFAULT)
        val decryptedBytes = cipher.doFinal(encryptedBytes)
        return String(decryptedBytes)
    }

    private fun getPrivateKey(context: Context): java.security.PrivateKey? {
        val sharedPreferences = context.getSharedPreferences("secret_shared_prefs", Context.MODE_PRIVATE)
        val privateKeyString = sharedPreferences.getString("private_key", null) ?: return null
        val privateKeyBytes = Base64.decode(privateKeyString, Base64.DEFAULT)
        return java.security.KeyFactory.getInstance("RSA").generatePrivate(X509EncodedKeySpec(privateKeyBytes))
    }

    private fun createOkHttpClient(context: Context, certificateRawResource: Int): OkHttpClient {
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
}
