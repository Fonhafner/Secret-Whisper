package com.example.secretwhisper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.secretwhisper.ui.theme.SecretWhisperTheme

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SecretWhisperTheme {
                LoginScreen()
            }
        }
    }
}

@Composable
fun LoginScreen() {
    var login by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Авторизация",
            fontSize = 32.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        TextField(
            value = login,
            onValueChange = { login = it },
            placeholder = { Text("Логин") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = password,
            onValueChange = { password = it },
            placeholder = { Text("Пароль") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = {
                // Ваш код авторизации
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Авторизироваться")
        }
    }
}
