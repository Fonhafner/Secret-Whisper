package com.example.secret_whisper

import android.content.Intent
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ViewGroup
import java.io.InputStream
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.example.secret_whisper.LoginActivity
import kotlin.math.log


class WelcomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

       //installCertificate(this)
        //val sslSocketFactory = getSSLSocketFactory()
        //val SocketFactory = getCustomSSLContext()

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

        // Добавляем кнопку "Продолжить"
        val continueButton = Button(this)
        continueButton.id = R.id.continueButton
        continueButton.text = "Продолжить"
        continueButton.setOnClickListener {
            val intent = Intent(this, AuthActivity::class.java)
            startActivity(intent)
            finish() // Опционально, если вы хотите закрыть WelcomeActivity после перехода
        }
        layout.addView(continueButton)

        // Добавляем кнопку "Войти"
        val loginButton = Button(this)
        loginButton.id = R.id.loginButton
        loginButton.text = "Войти"
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
            ConstraintSet.TOP
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

        // Привязываем кнопку "Продолжить" к верхней части экрана
        constraintSet.connect(
            continueButton.id,
            ConstraintSet.TOP,
            welcomeMessageTextView.id,
            ConstraintSet.BOTTOM,
            20
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

        // Центрируем кнопку "Войти" по горизонтали и выравниваем ее по вертикали между кнопкой "Продолжить" и нижним краем экрана
        constraintSet.centerHorizontally(loginButton.id, ConstraintSet.PARENT_ID)
        constraintSet.connect(
            loginButton.id,
            ConstraintSet.TOP,
            continueButton.id,
            ConstraintSet.BOTTOM,
            20
        )
        constraintSet.connect(
            loginButton.id,
            ConstraintSet.BOTTOM,
            ConstraintSet.PARENT_ID,
            ConstraintSet.BOTTOM,
            100
        )

        // Применяем настройки ConstraintSet
        constraintSet.applyTo(layout)

    }
}