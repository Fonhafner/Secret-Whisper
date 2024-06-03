package com.example.secretwhisper

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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

class AuthActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val okHttpClient = createOkHttpClient(context = this, certificateRawResource = R.raw.sertifi)
        setContent {
            AuthScreen(okHttpClient)
        }
    }
}

@Composable
fun AuthScreen(okHttpClient: OkHttpClient) {
    val navController = rememberNavController()
    NavHost(navController, startDestination = "login") {
        composable("login") {
            LoginPage(navController, okHttpClient)
        }
        composable("password") {
            PasswordPage(navController)
        }
        composable("recovery_code") {
            RecoveryCodePage(navController)
        }
    }
}

@Composable
fun LoginPage(navController: NavController, okHttpClient: OkHttpClient) {
    var username by remember { mutableStateOf(TextFieldValue()) }
    var showError by remember { mutableStateOf(false) }

    fun handleLogin() {
        val login = username.text
        val json = JSONObject().apply {
            put("username", login)
        }
        val requestBody = RequestBody.create(
            "application/json; charset=utf-8".toMediaType(),
            json.toString()
        )
        val request = Request.Builder()
            .url("https://rtusecretwhisper.ru/api/check-user-exists")
            .post(requestBody)
            .build()

        println("Sending request to: ${request.url}")

        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Обработка ошибки в случае неудачного запроса
                println("Error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                // Обработка ответа от сервера
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    val json = JSONObject(responseBody)
                    val userExists = json.getBoolean("exists")
                    if (userExists) {
                        // Пользователь существует, установить флаг ошибки
                        showError = true
                    } else {
                        // Пользователь не существует, продолжить навигацию на следующий экран
                        showError = false // Сбросить флаг ошибки
                        println("Response: $responseBody")
                        (navController.context as? ComponentActivity)?.runOnUiThread {
                            navController.navigate("password")
                        }
                    }
                } else {
                    // Обработка ошибки в случае неуспешного запроса
                    println("Error: ${response.code}")
                }
            }
        })
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Введите логин", fontSize = 20.sp)
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = username,
            onValueChange = { username = it },
            label = { Text(text = "Логин") },
            modifier = Modifier.fillMaxWidth()
        )
        if (showError) {
            Text(
                text = "Такой пользователь уже существует",
                color = MaterialTheme.colors.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            showError = false // Сбросить флаг ошибки перед следующей попыткой
            handleLogin()
        }) {
            Text(text = "Продолжить")
        }
    }
}

@Composable
fun PasswordPage(navController: NavController) {
    // Код для PasswordPage
}

@Composable
fun RecoveryCodePage(navController: NavController) {
    // Код для RecoveryCodePage
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
