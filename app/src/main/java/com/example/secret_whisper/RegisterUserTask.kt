package com.example.secret_whisper

import android.os.AsyncTask
import okhttp3.OkHttpClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Request
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class RegisterUserTask(
    private val client: OkHttpClient,
    private val callback: (String) -> Unit
) : AsyncTask<String, Void, String>() {

    override fun doInBackground(vararg params: String): String {
        val username = params[0]
        val password = params[1]
        val url = "https://rtusecretwhisper.ru/api/register"
        val json = JSONObject()
        json.put("username", username)
        json.put("password", password)

        val requestBody = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        try {
            val response = client.newCall(request).execute()
            return response.body?.string() ?: ""
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return ""
    }

    override fun onPostExecute(result: String) {
        super.onPostExecute(result)
        try {
            val json = JSONObject(result)
            // Handle response data
            if (json.has("error")) {
                val errorMessage = json.getString("error")
                callback(errorMessage)
            } else {
                val successMessage = "Registration Successful"
                callback(successMessage)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
            callback("An error occurred")
        }
    }
}
