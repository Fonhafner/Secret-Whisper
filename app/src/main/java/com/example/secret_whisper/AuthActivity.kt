package com.example.secret_whisper

import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.OkHttpClient

class AuthActivity : AppCompatActivity() {

    private lateinit var loginEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var continueButton: Button
    private lateinit var responseTextView: TextView
    private lateinit var client: OkHttpClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = RelativeLayout(this)
        setContentView(layout)

        loginEditText = EditText(this).apply {
            id = R.id.loginEditText
            hint = "Логин"
            layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(16, 16, 16, 0)
            }
        }
        layout.addView(loginEditText)

        passwordEditText = EditText(this).apply {
            id = R.id.passwordEditText
            hint = "Пароль"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(16, 16, 16, 0)
                addRule(RelativeLayout.BELOW, loginEditText.id)
            }
        }
        layout.addView(passwordEditText)

        confirmPasswordEditText = EditText(this).apply {
            id = R.id.confirmPasswordEditText
            hint = "Подтвердите пароль"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(16, 16, 16, 0)
                addRule(RelativeLayout.BELOW, passwordEditText.id)
            }
        }
        layout.addView(confirmPasswordEditText)

        responseTextView = TextView(this).apply {
            id = R.id.responseTextView
            layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(16, 16, 16, 0)
                addRule(RelativeLayout.BELOW, confirmPasswordEditText.id)
            }
        }
        layout.addView(responseTextView)

        continueButton = Button(this).apply {
            id = R.id.continueButton
            text = "Continue"
            layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(16, 16, 16, 0)
                addRule(RelativeLayout.BELOW, responseTextView.id)
                addRule(RelativeLayout.CENTER_HORIZONTAL)
            }
            setOnClickListener {
                val username = loginEditText.text.toString()
                val password = passwordEditText.text.toString()
                val confirmPassword = confirmPasswordEditText.text.toString()

                if (password == confirmPassword) {
                    RegisterUserTask(client) { result ->
                        responseTextView.text = result
                    }.execute(username, password)
                } else {
                    showToast("Passwords do not match")
                }
            }
        }
        layout.addView(continueButton)

        client = initializeOkHttpClient(this)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
