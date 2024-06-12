package com.example.secretwhisper

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class SplashScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Проверяем, авторизован ли пользователь
        val isLoggedIn = checkUserLoggedIn()

        // Определяем, на какой экран направить пользователя
        val destinationActivity =
            if (isLoggedIn) {
                MainActivity::class.java // Например, MainActivity для авторизованных пользователей
            } else {
                WelcomeActivity::class.java // Или WelcomeActivity для незарегистрированных пользователей
            }

        // Запускаем соответствующий экран
        startActivity(Intent(this, destinationActivity))
        finish()
    }

    private fun checkUserLoggedIn(): Boolean {
        // Проверяем наличие токена в SharedPreferences
        val sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE)
        val token = sharedPreferences.getString("token", null)
        return token != null
    }
}
