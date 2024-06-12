package com.example.secretwhisper

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.shape.RoundedCornerShape
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
    val context = LocalContext.current

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFE5E5E5)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top // Размещаем содержимое вверху
        ) {
            // Column для Image и Text
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 64.dp, bottom = 32.dp) // Отступы сверху и снизу для разделения
            ) {
                // Image
                val imageModifier = Modifier
                    .size(width = 262.dp, height = 271.dp)
                Image(
                    painter = painterResource(id = R.drawable.illustration), // Замените на свой ресурс изображения
                    contentDescription = "Welcome Image",
                    modifier = imageModifier
                )

                // Text "Общайся с кем угодно, где угодно"
                Text(
                    text = "Общайся с кем угодно,",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp) // Отступ сверху для разделения от изображения
                )
                Text(
                    text = "где угодно",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Spacer для отступа между верхней частью и нижней частью экрана
            Spacer(modifier = Modifier.weight(1f))

            // Button
            val buttonModifier = Modifier
                .width(327.dp)
                .height(60.dp)
                .padding(horizontal = 16.dp)
            Button(
                onClick = {
                    // Navigate to AuthActivity
                    val intent = Intent(context, AuthActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    context.startActivity(intent)
                    (context as? ComponentActivity)?.finish()
                },
                modifier = buttonModifier,
                shape = RoundedCornerShape(30.dp), // Углы кнопки с радиусом 30dp
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(0xFF002DE3), // Цвет фона кнопки
                    contentColor = Color(0xFFF7F7FC) // Цвет текста кнопки
                )
            ) {
                Text(text = "Начать общение")
            }

            // Clickable Text
            ClickableText(
                text = AnnotatedString("Уже есть аккаунт?"),
                style = TextStyle(
                    color = Color(0xFF0F1828), // Темно-синий цвет текста ссылки
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                ),
                onClick = {
                    // Navigate to LoginActivity
                    val intent = Intent(context, LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    context.startActivity(intent)
                    (context as? ComponentActivity)?.finish()
                },
                modifier = Modifier.padding(top = 16.dp, bottom = 32.dp) // Отступ сверху и снизу для разделения от кнопки
            )
        }
    }
}
