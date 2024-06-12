package com.example.secretwhisper


import android.content.Context

object TokenManager {
    fun saveToken(context: Context, token: String) {
        val sharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("token", token)
        editor.apply()
    }

    fun clearToken(context: Context) {
        val sharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove("token")
        editor.apply()
    }

    fun getToken(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
        return sharedPreferences.getString("token", null)
    }

    fun isUserLoggedIn(context: Context): Boolean {
        return getToken(context) != null
    }
}
