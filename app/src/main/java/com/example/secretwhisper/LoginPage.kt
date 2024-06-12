import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.secretwhisper.ui.theme.SecretWhisperTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

@Composable
fun LoginPage(navController: NavController, okHttpClient: OkHttpClient) {
    var username by remember { mutableStateOf(TextFieldValue()) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

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
                (navController.context as? ComponentActivity)?.runOnUiThread {
                    showError = true
                    errorMessage = "Ошибка сети. Попробуйте еще раз."
                }
            }

            override fun onResponse(call: Call, response: Response) {
                // Обработка ответа от сервера
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    val userExists = JSONObject(responseBody).getBoolean("exists")
                    if (userExists) {
                        // Пользователь существует, установить флаг ошибки
                        (navController.context as? ComponentActivity)?.runOnUiThread {
                            showError = true
                            errorMessage = "Такой пользователь уже существует"
                        }
                    } else {
                        // Пользователь не существует, продолжить навигацию на следующий экран
                        (navController.context as? ComponentActivity)?.runOnUiThread {
                            showError = false // Сбросить флаг ошибки
                            navController.navigate("password/$login")
                        }
                    }
                } else {
                    // Обработка ошибки в случае неуспешного запроса
                    println("Error: ${response.code}")
                    (navController.context as? ComponentActivity)?.runOnUiThread {
                        showError = true
                        errorMessage = "Ошибка сервера. Попробуйте еще раз."
                    }
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
                text = errorMessage,
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