package com.example.secretwhisper

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.secretwhisper.ui.theme.SecretWhisperTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.security.KeyStore
import java.security.SecureRandom
import java.security.cert.CertificateFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

class LoginActivity : ComponentActivity() {
    private val okHttpClient: OkHttpClient by lazy {
        createOkHttpClient(this, R.raw.sertifi)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SecretWhisperTheme {
                LoginScreen()
            }
        }
    }
}

@Composable
fun LoginScreen() {
    var login by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Top, // Размещаем содержимое вверху
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Заголовок "Авторизация"
        Text(
            text = "Авторизация",
            fontSize = 32.sp,
            color = Color(0xFF0F1828), // Цвет текста
            modifier = Modifier.padding(top = 60.dp, bottom = 32.dp) // Отступ сверху и снизу
        )

        // Поле ввода логина
        TextField(
            value = login,
            onValueChange = { login = it },
            placeholder = { Text("Логин") },
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color(0xFFF7F7FC)) // Заливка поля ввода логина
                .padding(bottom = 16.dp),
            textStyle = LocalTextStyle.current.copy(color = Color(0xFFADB5BD)) // Цвет текста в поле ввода логина
        )

        // Поле ввода пароля
        TextField(
            value = password,
            onValueChange = { password = it },
            placeholder = { Text("Пароль") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Отображение сообщения об ошибке, если есть
        if (errorMessage.isNotBlank()) {
            Text(
                text = errorMessage,
                color = MyColors.errorColor,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Кнопка "Авторизироваться"
        Button(
            onClick = {
                errorMessage = ""
                loginUser(context, login, password) { error, token ->
                    if (token != null) {
                        saveToken(context, token)
                        // Генерация и отправка ключей
                        generateAndSendKeys(context, login)
                        // Переход на MainActivity после успешной авторизации
                        val intent = Intent(context, MainActivity::class.java)
                        context.startActivity(intent)
                    } else {
                        errorMessage = error ?: ""
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(horizontal = 32.dp), // Добавляем отступы по горизонтали для кнопки
            shape = MaterialTheme.shapes.medium.copy(
                topStart = CornerSize(30.dp),
                topEnd = CornerSize(30.dp),
                bottomStart = CornerSize(30.dp),
                bottomEnd = CornerSize(30.dp)
            ) // Закругления углов
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = Color(0xFF002DE3), shape = MaterialTheme.shapes.medium),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Авторизироваться",
                    color = Color(0xFFF7F7FC), // Цвет текста на кнопке
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}






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

fun loginUser(context: Context, username: String, password: String, callback: (String?, String?) -> Unit) {
    val okHttpClient = createOkHttpClient(context, R.raw.sertifi)
    val json = JSONObject().apply {
        put("username", username)
        put("password", password)
    }
    val requestBody = json.toString().toRequestBody("application/json".toMediaType())
    val request = Request.Builder()
        .url("https://rtusecretwhisper.ru/api/auth/login")
        .post(requestBody)
        .build()

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = okHttpClient.newCall(request).execute()
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    val jsonResponse = JSONObject(responseBody)
                    val token = jsonResponse.getString("token")
                    callback(null, token)
                } else {
                    val errorMessage = response.body?.string() ?: "Неизвестная ошибка"
                    Log.e("LoginActivity", "Ошибка при авторизации: $errorMessage")
                    callback(errorMessage, null)
                }
            }
        } catch (e: IOException) {
            Log.e("LoginActivity", "Ошибка сети при авторизации", e)
            callback("Ошибка сети при авторизации", null)
        }
    }
}

fun generateAndSendKeys(context: Context, username: String) {
    val keyPair = generateKeyPair()
    saveKeysToSharedPreferences(context, keyPair.private, keyPair.public)
    val publicKeyString = Base64.encodeToString(keyPair.public.encoded, Base64.DEFAULT)
    sendPublicKeyToServer(username, publicKeyString)
}

private fun saveToken(context: Context, token: String) {
    val sharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    editor.putString("token", token)
    editor.apply()
}

fun sendPublicKeyToServer(username: String, publicKey: String) {
    val json = JSONObject().apply {
        put("username", username)
        put("public_key", publicKey)
    }
    val requestBody = json.toString().toRequestBody("application/json".toMediaType())
    val request = Request.Builder()
        .url("https://rtusecretwhisper.ru/api/save_public_key")
        .post(requestBody)
        .build()

    val okHttpClient = OkHttpClient.Builder().build()
    okHttpClient.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            Log.e("LoginActivity", "Ошибка при отправке открытого ключа", e)
        }

        override fun onResponse(call: Call, response: Response) {
            if (response.isSuccessful) {
                Log.d("LoginActivity", "Открытый ключ успешно отправлен на сервер")
            } else {
                Log.e("LoginActivity", "Ошибка при отправке открытого ключа: ${response.code}")
            }
        }
    })
}

object MyColors {
    val errorColor = Color.Red // Пример цвета для отображения ошибок
    // Другие цвета
}
