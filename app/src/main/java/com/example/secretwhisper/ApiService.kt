package com.example.secretwhisper



import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Request
import java.io.IOException

fun accessProtectedResource(context: Context, callback: (String?) -> Unit) {
    val sharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
    val token = sharedPreferences.getString("token", null)

    if (token == null) {
        callback("Пользователь не авторизован")
        return
    }

    val okHttpClient = createOkHttpClient(context, R.raw.sertifi)
    val request = Request.Builder()
        .url("https://rtusecretwhisper.ru/api/protected")
        .header("Authorization", "Bearer $token")
        .get()
        .build()

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = okHttpClient.newCall(request).execute()
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    callback(responseBody)
                } else {
                    val errorMessage = response.body?.string() ?: "Неизвестная ошибка"
                    Log.e("ProtectedResource", "Ошибка при доступе к ресурсу: $errorMessage")
                    callback(errorMessage)
                }
            }
        } catch (e: IOException) {
            Log.e("ProtectedResource", "Ошибка при выполнении запроса: ${e.message}", e)
            withContext(Dispatchers.Main) {
                callback("Ошибка при выполнении запроса")
            }
        }
    }
}

