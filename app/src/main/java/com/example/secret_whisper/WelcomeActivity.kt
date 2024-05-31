package com.example.secret_whisper

import android.content.Intent
import android.graphics.Typeface
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.example.secret_whisper.LoginActivity

class WelcomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Создаем ConstraintLayout в качестве корневого элемента макета
        val layout = ConstraintLayout(this)
        layout.layoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.MATCH_PARENT
        )
        setContentView(layout)

        // Добавляем TextView для приветствия
        val welcomeMessageTextView = TextView(this)
        welcomeMessageTextView.id = R.id.welcomeMessageTextView
        welcomeMessageTextView.text = "Добро пожаловать!"
        welcomeMessageTextView.textSize = 24f
        welcomeMessageTextView.setTypeface(null, Typeface.BOLD)
        layout.addView(welcomeMessageTextView)

        // Добавляем кнопку "Начать общение"
        val continueButton = Button(this)
        continueButton.id = R.id.continueButton
        continueButton.text = "Начать общение"
        continueButton.setOnClickListener {
            val intent = Intent(this, AuthActivity::class.java)
            startActivity(intent)
            finish() // Опционально, если вы хотите закрыть WelcomeActivity после перехода
        }
        layout.addView(continueButton)

        // Добавляем ссылку "Уже есть аккаунт?"
        val loginButton = TextView(this)
        loginButton.id = R.id.loginButton
        loginButton.text = "Уже есть аккаунт?"
        loginButton.setTextColor(Color.BLUE)
        loginButton.textSize = 16f
        loginButton.setTypeface(null, Typeface.BOLD)
        loginButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish() // Опционально, если вы хотите закрыть WelcomeActivity после перехода
        }
        layout.addView(loginButton)

        // Настраиваем ConstraintSet для размещения элементов
        val constraintSet = ConstraintSet()
        constraintSet.clone(layout)

        // Привязываем TextView к верхней части экрана
        constraintSet.connect(
            welcomeMessageTextView.id,
            ConstraintSet.TOP,
            ConstraintSet.PARENT_ID,
            ConstraintSet.TOP,
            100
        )
        constraintSet.connect(
            welcomeMessageTextView.id,
            ConstraintSet.START,
            ConstraintSet.PARENT_ID,
            ConstraintSet.START
        )
        constraintSet.connect(
            welcomeMessageTextView.id,
            ConstraintSet.END,
            ConstraintSet.PARENT_ID,
            ConstraintSet.END
        )

        // Привязываем кнопку "Начать общение" к нижней части экрана
        constraintSet.connect(
            continueButton.id,
            ConstraintSet.BOTTOM,
            ConstraintSet.PARENT_ID,
            ConstraintSet.BOTTOM,
            200
        )
        constraintSet.connect(
            continueButton.id,
            ConstraintSet.START,
            ConstraintSet.PARENT_ID,
            ConstraintSet.START,
            100
        )
        constraintSet.connect(
            continueButton.id,
            ConstraintSet.END,
            ConstraintSet.PARENT_ID,
            ConstraintSet.END,
            100
        )

        // Центрируем ссылку "Уже есть аккаунт?" под кнопкой "Начать общение"
        constraintSet.centerHorizontally(loginButton.id, ConstraintSet.PARENT_ID)
        constraintSet.connect(
            loginButton.id,
            ConstraintSet.TOP,
            continueButton.id,
            ConstraintSet.BOTTOM,
            20
        )

        // Применяем настройки ConstraintSet
        constraintSet.applyTo(layout)
    }
}
