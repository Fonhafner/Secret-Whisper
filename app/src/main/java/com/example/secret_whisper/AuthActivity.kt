package com.example.secret_whisper

import android.os.Bundle
import android.view.View.generateViewId
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class AuthActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Создаем ConstraintLayout в качестве корневого элемента макета
        val layout = ConstraintLayout(this)
        layout.layoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.MATCH_PARENT
        )
        setContentView(layout)

        // Создаем EditText для ввода логина
        val loginEditText = EditText(this).apply {
            id = generateViewId()
            hint = "Логин"
        }
        layout.addView(loginEditText)

        // Создаем EditText для ввода пароля
        val passwordEditText = EditText(this).apply {
            id = generateViewId()
            hint = "Пароль"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        layout.addView(passwordEditText)

        // Создаем EditText для подтверждения пароля
        val confirmPasswordEditText = EditText(this).apply {
            id = generateViewId()
            hint = "Подтвердите пароль"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        layout.addView(confirmPasswordEditText)

        // Создаем кнопку "Продолжить"
        val continueButton = Button(this).apply {
            id = generateViewId()
            text = "Продолжить"
        }
        layout.addView(continueButton)

        // Настраиваем ConstraintSet для размещения элементов
        val constraintSet = ConstraintSet().apply {
            clone(layout)

            connect(
                loginEditText.id,
                ConstraintSet.TOP,
                ConstraintSet.PARENT_ID,
                ConstraintSet.TOP,
                100
            )
            connect(
                loginEditText.id,
                ConstraintSet.START,
                ConstraintSet.PARENT_ID,
                ConstraintSet.START,
                50
            )
            connect(
                loginEditText.id,
                ConstraintSet.END,
                ConstraintSet.PARENT_ID,
                ConstraintSet.END,
                50
            )

            connect(
                passwordEditText.id,
                ConstraintSet.TOP,
                loginEditText.id,
                ConstraintSet.BOTTOM,
                20
            )
            connect(
                passwordEditText.id,
                ConstraintSet.START,
                ConstraintSet.PARENT_ID,
                ConstraintSet.START,
                50
            )
            connect(
                passwordEditText.id,
                ConstraintSet.END,
                ConstraintSet.PARENT_ID,
                ConstraintSet.END,
                50
            )

            connect(
                confirmPasswordEditText.id,
                ConstraintSet.TOP,
                passwordEditText.id,
                ConstraintSet.BOTTOM,
                20
            )
            connect(
                confirmPasswordEditText.id,
                ConstraintSet.START,
                ConstraintSet.PARENT_ID,
                ConstraintSet.START,
                50
            )
            connect(
                confirmPasswordEditText.id,
                ConstraintSet.END,
                ConstraintSet.PARENT_ID,
                ConstraintSet.END,
                50
            )

            connect(
                continueButton.id,
                ConstraintSet.TOP,
                confirmPasswordEditText.id,
                ConstraintSet.BOTTOM,
                50
            )
            connect(
                continueButton.id,
                ConstraintSet.START,
                ConstraintSet.PARENT_ID,
                ConstraintSet.START,
                100
            )
            connect(
                continueButton.id,
                ConstraintSet.END,
                ConstraintSet.PARENT_ID,
                ConstraintSet.END,
                100
            )

            applyTo(layout)
        }

        continueButton.setOnClickListener {
            val username = loginEditText.text.toString()
            val password = passwordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()

            // Проверка на совпадение паролей
            if (password != confirmPassword) {
                Toast.makeText(this, "Пароли не совпадают", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Создание JSON-объекта с данными пользователя
            val jsonBody = JSONObject().apply {
                put("username", username)
                put("password", password)
            }

            // Отправка POST-запроса на сервер
            val queue = Volley.newRequestQueue(this)
            val url = "https://87.242.119.51//api/register" // Замените на ваш IP-адрес и эндпоинт
            val request = JsonObjectRequest(
                Request.Method.POST, url, jsonBody,
                Response.Listener { response ->
                    // Обработка ответа от сервера
                    Toast.makeText(this, "Пользователь успешно зарегистрирован", Toast.LENGTH_SHORT).show()
                },
                Response.ErrorListener { error ->
                    // Обработка ошибки
                    Toast.makeText(this, "Ошибка: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            )
            queue.add(request)
        }
    }
}
