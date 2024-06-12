import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

@Composable
fun PasswordPage(navController: NavController, username: String, okHttpClient: OkHttpClient) {
    var password by remember { mutableStateOf(TextFieldValue()) }
    var confirmPassword by remember { mutableStateOf(TextFieldValue()) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    fun handlePassword() {
        if (password.text != confirmPassword.text) {
            showError = true
            errorMessage = "Пароли не совпадают"
        } else {
            val json = JSONObject().apply {
                put("username", username)
                put("password", password.text)
            }
            val requestBody = RequestBody.create(
                "application/json; charset=utf-8".toMediaType(),
                json.toString()
            )
            val request = Request.Builder()
                .url("https://rtusecretwhisper.ru/api/register")
                .post(requestBody)
                .build()

            println("Sending registration request to: ${request.url}")

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
                        println("Registration successful: $responseBody")
                        // Переход на страницу кода восстановления после успешной регистрации
                        (navController.context as? ComponentActivity)?.runOnUiThread {
                            navController.navigate("recovery_code/" + JSONObject(responseBody).getString("recovery_key"))
                        }
                    } else {
                        println("Error: ${response.code}")
                        (navController.context as? ComponentActivity)?.runOnUiThread {
                            showError = true
                            errorMessage = "Ошибка сервера. Попробуйте еще раз."
                        }
                    }
                }
            })
        }
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Введите пароль для $username", fontSize = 20.sp)
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(text = "Пароль") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text(text = "Подтвердите пароль") },
            visualTransformation = PasswordVisualTransformation(),
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
        Button(onClick = { handlePassword() }) {
            Text(text = "Продолжить")
        }
    }
}
