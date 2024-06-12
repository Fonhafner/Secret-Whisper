package com.example.secretwhisper

import LoginPage
import PasswordPage
import RecoveryCodePage
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import createOkHttpClient
import okhttp3.OkHttpClient

class AuthActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val okHttpClient = createOkHttpClient(context = this, certificateRawResource = R.raw.sertifi)
        setContent {
            AuthScreen(okHttpClient)
        }
    }
}

@Composable
fun AuthScreen(okHttpClient: OkHttpClient) {
    val navController = rememberNavController()
    NavHost(navController, startDestination = "login") {
        composable("login") {
            LoginPage(navController, okHttpClient)
        }
        composable("password/{username}", arguments = listOf(navArgument("username") { defaultValue = "" })) { backStackEntry ->
            PasswordPage(navController, backStackEntry.arguments?.getString("username") ?: "", okHttpClient)
        }
        composable("recovery_code/{recoveryCode}") { backStackEntry ->
            RecoveryCodePage(navController, backStackEntry.arguments?.getString("recoveryCode") ?: "")
        }
    }
}
