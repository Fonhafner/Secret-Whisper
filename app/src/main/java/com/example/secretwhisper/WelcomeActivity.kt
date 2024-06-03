package com.example.secretwhisper

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.secretwhisper.ui.theme.SecretWhisperTheme

class WelcomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SecretWhisperTheme {
                WelcomeScreen()
            }
        }
    }
}

@Composable
fun WelcomeScreen() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Добро пожаловать!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            val context = LocalContext.current
            Button(
                onClick = {
                    // Navigate to AuthActivity
                    val intent = Intent(context, AuthActivity::class.java)
                    context.startActivity(intent)
                    (context as? ComponentActivity)?.finish()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(text = "Начать общение")
            }

            Spacer(modifier = Modifier.height(8.dp))

            ClickableText(
                text = AnnotatedString("Уже есть аккаунт?"),
                style = TextStyle(
                    color = Color.Blue,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                ),
                onClick = {
                    // Navigate to LoginActivity
                    val intent = Intent(context, LoginActivity::class.java)
                    context.startActivity(intent)
                    (context as? ComponentActivity)?.finish()
                }
            )
        }
    }
}
