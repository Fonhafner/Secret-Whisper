package com.example.secret_whisper

import android.os.AsyncTask
import android.os.Bundle
import android.text.InputType
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.security.KeyStore
import java.security.cert.CertificateFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody


class AuthActivity : AppCompatActivity() {

    private lateinit var loginEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var continueButton: Button
    private lateinit var client: OkHttpClient
    private lateinit var responseTextView: TextView // Добавлено

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = RelativeLayout(this)
        setContentView(layout)

        loginEditText = EditText(this).apply {
            id = R.id.loginEditText
            hint = "Username"
            layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(16, 16, 16, 0)
            }
            layout.addView(this)
        }

        passwordEditText = EditText(this).apply {
            id = R.id.passwordEditText
            hint = "Password"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(16, 16, 16, 0)
                addRule(RelativeLayout.BELOW, loginEditText.id)
            }
            layout.addView(this)
        }

        confirmPasswordEditText = EditText(this).apply {
            id = R.id.confirmPasswordEditText
            hint = "Confirm Password"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(16, 16, 16, 0)
                addRule(RelativeLayout.BELOW, passwordEditText.id)
            }
            layout.addView(this)
        }

        responseTextView = TextView(this).apply { // Добавлено
            id = R.id.responseTextView
            layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(16, 16, 16, 0)
                addRule(RelativeLayout.BELOW, confirmPasswordEditText.id)
            }
            layout.addView(this)
        }

        continueButton = Button(this).apply {
            id = R.id.continueButton
            text = "Continue"
            layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(16, 16, 16, 0)
                addRule(RelativeLayout.BELOW, responseTextView.id) // Изменено
                addRule(RelativeLayout.CENTER_HORIZONTAL)
            }
            setOnClickListener {
                val username = loginEditText.text.toString()
                val password = passwordEditText.text.toString()
                val confirmPassword = confirmPasswordEditText.text.toString()

                if (password == confirmPassword) {
                    RegisterUserTask().execute(username, password)
                } else {
                    showToast("Passwords do not match")
                }
            }
            layout.addView(this)
        }

        // Загрузка сертификата из raw ресурсов
        val certificateInputStream: InputStream = resources.openRawResource(R.raw.sertifi)
        val certificateFactory = CertificateFactory.getInstance("X.509")
        val certificate = certificateFactory.generateCertificate(certificateInputStream)

        // Создание хранилища ключей KeyStore и добавление сертификата в него
        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
        keyStore.load(null, null)
        keyStore.setCertificateEntry("certificate", certificate)

        // Создание менеджера доверия, используя TrustManagerFactory
        val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        trustManagerFactory.init(keyStore)
        val trustManagers = trustManagerFactory.trustManagers

        // Получение X509TrustManager из массива TrustManager'ов
        val trustManager = trustManagers[0] as X509TrustManager

        // Создание SSLContext и настройка его с использованием X509TrustManager
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, arrayOf(trustManager), null)

        // Создание OkHttpClient с настройками SSLContext и TrustManager'а
        client = OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, trustManager)
            .build()
    }

    private inner class RegisterUserTask : AsyncTask<String, Void, String>() {

        override fun doInBackground(vararg params: String): String {
            val username = params[0]
            val password = params[1]
            val url = "https://rtusecretwhisper.ru/api/register"
            val json  = JSONObject()
            json.put("username", username)
            json.put("password", password)

            val requestBody = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            try {
                val response = client.newCall(request).execute()
                return response.body?.string() ?: ""
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return ""
        }

        override fun onPostExecute(result: String) {
            super.onPostExecute(result)
            try {
                val json = JSONObject(result)
                // Handle response data
                if (json.has("error")) {
                    val errorMessage = json.getString("error")
                    responseTextView.text = errorMessage // Изменено
                } else {
                    val successMessage = "Registration Successful" // Добавлено
                    responseTextView.text = successMessage // Добавлено
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

