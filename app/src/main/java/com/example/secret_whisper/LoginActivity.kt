package com.example.secret_whisper

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Создаем ConstraintLayout в качестве корневого элемента макета
        val layout = ConstraintLayout(this)
        layout.layoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.MATCH_PARENT
        )
        setContentView(layout)

        // Добавляем EditText для ввода логина
        val loginEditText = EditText(this)
        loginEditText.id = R.id.loginEditText
        loginEditText.hint = "Логин"
        layout.addView(loginEditText)

        // Добавляем EditText для ввода пароля
        val passwordEditText = EditText(this)
        passwordEditText.id = R.id.passwordEditText
        passwordEditText.hint = "Пароль"
        passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        layout.addView(passwordEditText)

        // Добавляем кнопку "Войти"
        val loginButton = Button(this)
        loginButton.id = R.id.loginButton
        loginButton.text = "Войти"
        loginButton.setOnClickListener {
            val intent = Intent(this, WelcomeActivity::class.java)
            startActivity(intent)
            finish() // Опционально, если вы хотите закрыть WelcomeActivity после перехода
        }
        layout.addView(loginButton)

        // Добавляем текст "Забыли пароль?"
        val forgotPasswordTextView = TextView(this)
        forgotPasswordTextView.id = R.id.forgotPasswordTextView
        forgotPasswordTextView.text = "Забыли пароль?"
        forgotPasswordTextView.setOnClickListener {
            // Обработчик нажатия на текст "Забыли пароль?"
            // Здесь можно добавить код для перехода на экран восстановления пароля
        }
        layout.addView(forgotPasswordTextView)

        // Настраиваем ConstraintSet для размещения элементов
        val constraintSet = ConstraintSet()
        constraintSet.clone(layout)

        // Привязываем EditText для ввода логина к верхней части экрана
        constraintSet.connect(
            loginEditText.id,
            ConstraintSet.TOP,
            ConstraintSet.PARENT_ID,
            ConstraintSet.TOP,
            100
        )
        constraintSet.connect(
            loginEditText.id,
            ConstraintSet.START,
            ConstraintSet.PARENT_ID,
            ConstraintSet.START,
            32
        )
        constraintSet.connect(
            loginEditText.id,
            ConstraintSet.END,
            ConstraintSet.PARENT_ID,
            ConstraintSet.END,
            32
        )

        // Привязываем EditText для ввода пароля к нижней части EditText для ввода логина
        constraintSet.connect(
            passwordEditText.id,
            ConstraintSet.TOP,
            loginEditText.id,
            ConstraintSet.BOTTOM,
            16
        )
        constraintSet.connect(
            passwordEditText.id,
            ConstraintSet.START,
            ConstraintSet.PARENT_ID,
            ConstraintSet.START,
            32
        )
        constraintSet.connect(
            passwordEditText.id,
            ConstraintSet.END,
            ConstraintSet.PARENT_ID,
            ConstraintSet.END,
            32
        )

        // Привязываем кнопку "Войти" к нижней части EditText для ввода пароля
        constraintSet.connect(
            loginButton.id,
            ConstraintSet.TOP,
            passwordEditText.id,
            ConstraintSet.BOTTOM,
            32
        )
        constraintSet.connect(
            loginButton.id,
            ConstraintSet.START,
            ConstraintSet.PARENT_ID,
            ConstraintSet.START,
            32
        )
        constraintSet.connect(
            loginButton.id,
            ConstraintSet.END,
            ConstraintSet.PARENT_ID,
            ConstraintSet.END,
            32
        )

        // Привязываем текст "Забыли пароль?" к нижней части кнопки "Войти"
        constraintSet.connect(
            forgotPasswordTextView.id,
            ConstraintSet.TOP,
            loginButton.id,
            ConstraintSet.BOTTOM,
            16
        )
        constraintSet.connect(
            forgotPasswordTextView.id,
            ConstraintSet.START,
            ConstraintSet.PARENT_ID,
            ConstraintSet.START,
            32
        )
        constraintSet.connect(
            forgotPasswordTextView.id,
            ConstraintSet.END,
            ConstraintSet.PARENT_ID,
            ConstraintSet.END,
            32
        )

        // Применяем настройки ConstraintSet
        constraintSet.applyTo(layout)
    }
}