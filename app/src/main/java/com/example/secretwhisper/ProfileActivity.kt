package com.example.secretwhisper

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import java.io.IOException
import java.security.KeyStore
import java.security.SecureRandom
import java.security.cert.CertificateFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

class ProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Получаем имя пользователя из Intent
        val username = intent.getStringExtra("username") ?: ""

        setContent {
            ProfileScreen(username = username)
        }
    }
}

@Composable
fun ProfileScreen(username: String) {
    Surface(color = MaterialTheme.colors.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = username, style = MaterialTheme.typography.h5)
            Spacer(modifier = Modifier.height(16.dp))
            AddToContactsButton()
            Spacer(modifier = Modifier.height(16.dp))
            GoToChatButton(username = username)
        }
    }
}

@Composable
fun AddToContactsButton() {
    val context = LocalContext.current
    Button(
        onClick = {
            // Добавьте здесь логику для добавления пользователя в контакты
            // Например, отправьте запрос на сервер или выполните другие необходимые действия
        }
    ) {
        Text(text = "Добавить в контакты")
    }
}

@Composable
fun GoToChatButton(username: String) {
    val context = LocalContext.current
    Button(
        onClick = {
            // Получаем имя пользователя, отправляющего запрос, из SharedPreferences
            val loggedInUsername = getLoggedInUsername(context)
            loggedInUsername?.let { loggedInUser ->
                Log.d("ProfileActivity", "loggedInUser before sending request: $loggedInUser")
                Log.d("ProfileActivity", "username before sending request: $username")
                // Отправляем запрос на API для проверки существующего чата или создания нового
                val client = createOkHttpClient(context, R.raw.sertifi) // Замени R.raw.your_certificate на свой ресурс сертификата

                val json = JSONObject().apply {
                    put("senderUsername", loggedInUser)
                    put("receiverUsername", username)
                }
                val requestBody = json.toString()

                val request = Request.Builder()
                    .url("https://rtusecretwhisper.ru/api/create-or-check-chat")
                    .post(RequestBody.create("application/json".toMediaType(), requestBody))
                    .build()

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        e.printStackTrace()
                        // Обработка ошибки, если запрос не удалось выполнить
                    }

                    override fun onResponse(call: Call, response: Response) {
                        val responseData = response.body?.string() // Сохраняем тело ответа в переменную

                        if (response.isSuccessful) {
                            // Обработка успешного ответа
                            val chatIdJson = JSONObject(responseData)
                            val chatId = chatIdJson.getInt("chatId") // Получаем chatId из JSON

                            Log.d("ProfileActivity", "Logged in user: $loggedInUser")
                            val intent = Intent(context, ChatActivity::class.java).apply {
                                putExtra("username", username)
                                putExtra("chatId", chatId) // Передаем chatId в ChatActivity через интент
                            }
                            context.startActivity(intent)
                        } else {
                            // Обработка неуспешного ответа
                        }
                    }
                })
            }
        }
    ) {
        Text(text = "Перейти в чат с пользователем")
    }
}






// Получаем имя пользователя из SharedPreferences
fun getLoggedInUsername(context: Context): String? {
    val sharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
    val token = sharedPreferences.getString("token", null)
    var loggedInUsername: String? = null
    if (token != null) {
        val tokenParts = token.split(".")
        if (tokenParts.size == 3) {
            val payload = tokenParts[1]
            val decodedPayload = Base64.decode(payload, Base64.DEFAULT)
            val decodedString = String(decodedPayload, Charsets.UTF_8)
            Log.d("ProfileActivity", "Decoded token payload: $decodedString")

            // Извлекаем имя пользователя из JSON
            val jsonObject = JSONObject(decodedString)
            loggedInUsername = jsonObject.optString("username")
        }
    }
    Log.d("ProfileActivity", "Logged in username from SharedPreferences: $loggedInUsername")
    return loggedInUsername
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ProfileScreen(username = "Username")
}

