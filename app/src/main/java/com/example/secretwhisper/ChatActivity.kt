package com.example.secretwhisper

import android.content.Context
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.security.KeyFactory
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

class ChatActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val username = intent.getStringExtra("username") ?: ""
        val chatId = intent.getIntExtra("chatId", 0)

        Log.d("ChatActivity", "Username: $username, Chat ID: $chatId")

        setContent {
            val currentUsername = getLoggedInUsername(this) ?: ""
            var currentUserId by remember { mutableStateOf(-1) }
            val messages = remember { mutableStateOf(listOf<Message>()) }
            val privateKey = getPrivateKey(this) // Получаем закрытый ключ пользователя

            LaunchedEffect(currentUsername) {
                fetchUserId(this@ChatActivity, currentUsername) { userId ->
                    currentUserId = userId
                }
            }

            LaunchedEffect(Unit) {
                fetchMessages(this@ChatActivity, chatId) { fetchedMessages ->
                    messages.value = fetchedMessages
                }
            }

            val coroutineScope = rememberCoroutineScope()

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = "Чат с $username",
                                color = Color.Black,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        },
                        backgroundColor = Color.White,
                        elevation = AppBarDefaults.TopAppBarElevation
                    )
                },
                content = {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 16.dp)
                    ) {

                        ChatMessages(
                            chatId = chatId,
                            messages = messages.value,
                            currentUserId = currentUserId,
                            privateKey = privateKey,
                            context = this@ChatActivity
                        )
                    }
                },
                bottomBar = {
                    ChatInput(
                        chatId = chatId,
                        senderUsername = currentUsername,
                        receiverUsername = username,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(16.dp)
                    )
                }
            )

            LaunchedEffect(Unit) {
                while (true) {
                    fetchMessages(this@ChatActivity, chatId) { fetchedMessages ->
                        messages.value = fetchedMessages
                    }
                    delay(500L)
                }
            }
        }
    }

    private fun getLoggedInUsername(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("token", null)
        var loggedInUsername: String? = null
        if (token != null) {
            val tokenParts = token.split(".")
            if (tokenParts.size == 3) {
                val payload = tokenParts[1]
                val decodedPayload = Base64.decode(payload, Base64.DEFAULT)
                val decodedString = String(decodedPayload, Charsets.UTF_8)
                Log.d("ProfileActivity", "Decoded token payload: $decodedString")

                val jsonObject = JSONObject(decodedString)
                loggedInUsername = jsonObject.optString("username")
            }
        }
        Log.d("ProfileActivity", "Logged in username from SharedPreferences: $loggedInUsername")
        return loggedInUsername
    }

    private fun fetchUserId(context: Context, username: String, callback: (Int) -> Unit) {
        val client = OkHttpClient()

        val request = Request.Builder()
            .url("https://rtusecretwhisper.ru/api/getUserId/$username")
            .get()
            .build()

        val call = client.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("FetchUserId", "Failed to fetch user ID", e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    response.body?.string()?.let { responseData ->
                        val jsonObject = JSONObject(responseData)
                        val userId = jsonObject.optInt("user_id", -1)
                        Log.d("FetchUserId", "Fetched user ID: $userId")
                        callback(userId)
                    }
                } else {
                    Log.e("FetchUserId", "Failed to fetch user ID with response code: ${response.code}")
                }
            }
        })
    }

    // Function to get the private key from resources
    private fun getPrivateKey(context: Context): ByteArray {
        // Replace this with the actual logic to get the user's private key
        // For example, load it from secure storage or key management system
        return byteArrayOf()
    }
}


@Composable
fun ChatMessages(chatId: Int, messages: List<Message>, currentUserId: Int, privateKey: ByteArray, context: Context) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
            .verticalScroll(scrollState)
    ) {
        messages.forEach { message ->
            val encryptedKey = if (message.senderId == currentUserId) {
                message.senderEncryptedKey
            } else {
                message.receiverEncryptedKey
            }

            val decryptedMessage = decryptMessage(
                Base64.decode(message.encryptedMessage, Base64.DEFAULT),
                Base64.decode(encryptedKey, Base64.DEFAULT),
                Base64.decode(message.iv, Base64.DEFAULT), // Pass IV
                context // Use the passed context
            )

            MessageItem(
                text = decryptedMessage,
                isSentByUser = message.senderId == currentUserId
            )
        }

        LaunchedEffect(messages.size) {
            coroutineScope.launch {
                scrollState.animateScrollTo(scrollState.maxValue)
            }
        }
        Spacer(modifier = Modifier.height(65.dp))
    }
}


@Composable
fun MessageItem(text: String, isSentByUser: Boolean) {
    val backgroundColor = if (isSentByUser) Color(0xFF002DE3) else Color.White
    val textColor = if (isSentByUser) Color.White else Color.Black

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isSentByUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            modifier = Modifier.padding(8.dp),
            color = backgroundColor,
            elevation = 4.dp,
            shape = MaterialTheme.shapes.medium.copy(
                bottomStart = CornerSize(16.dp), // Round bottom corners
                bottomEnd = CornerSize(16.dp)
            )
        ) {
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .background(color = backgroundColor, shape = MaterialTheme.shapes.medium)
            ) {
                Text(
                    text = text,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Start,
                    color = textColor,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun ChatInput(
    chatId: Int,
    senderUsername: String,
    receiverUsername: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val messageState = remember { mutableStateOf(TextFieldValue()) }

    Surface(
        modifier = modifier,
        color = Color.White, // Set white background color
        elevation = 4.dp,
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Message input field
            TextField(
                value = messageState.value,
                onValueChange = { messageState.value = it },
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .height(52.dp),
                textStyle = LocalTextStyle.current.copy(fontSize = 16.sp),
                placeholder = { Text("Введите сообщение...") },
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.White, // Set white background color
                    cursorColor = Color(0xFF002DE3), // Set cursor color
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            // Send button
            IconButton(
                onClick = {
                    val message = messageState.value.text
                    if (message.isNotEmpty()) {
// Send message
                        sendMessage(context, chatId, senderUsername, receiverUsername, message)
                        // Clear message input
                        messageState.value = TextFieldValue()
                    }
                },
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Отправить",
                    tint = Color(0xFF002DE3) // Set button color
                )
            }
        }
    }
}


fun sendMessage(context: Context, chatId: Int, senderUsername: String, receiverUsername: String, message: String) {
    fetchPublicKey(context, receiverUsername) { receiverPublicKey ->
        receiverPublicKey?.let { publicKey ->
            val client = OkHttpClient()

            // Session key generation
            val keyGenerator = KeyGenerator.getInstance("AES")
            keyGenerator.init(256)
            val sessionKey = keyGenerator.generateKey()

            // Encrypt message with session key
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, sessionKey)
            val iv = cipher.iv
            val cipherText = cipher.doFinal(message.toByteArray())

            // Encrypt session key with receiver's public key
            val keyFactory = KeyFactory.getInstance("RSA")
            val receiverPublicKeySpec = X509EncodedKeySpec(publicKey)
            val receiverKey = keyFactory.generatePublic(receiverPublicKeySpec)
            val rsaCipherForReceiver = Cipher.getInstance("RSA/ECB/PKCS1Padding")
            rsaCipherForReceiver.init(Cipher.ENCRYPT_MODE, receiverKey)
            val encryptedSessionKeyForReceiver = rsaCipherForReceiver.doFinal(sessionKey.encoded)

            // Get sender's public key from EncryptedSharedPreferences
            val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
            val sharedPreferences = EncryptedSharedPreferences.create(
                "secret_shared_prefs",
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )

            val senderPublicKeyBase64 = sharedPreferences.getString("public_key", null)
            if (senderPublicKeyBase64 != null) {
                val senderPublicKeyBytes = Base64.decode(senderPublicKeyBase64, Base64.DEFAULT)
                val senderPublicKeySpec = X509EncodedKeySpec(senderPublicKeyBytes)
                val senderPublicKey = keyFactory.generatePublic(senderPublicKeySpec)

                // Log sender's public key
                Log.d("SendMessage", "Sender's Public Key: ${Base64.encodeToString(senderPublicKey.encoded, Base64.DEFAULT)}")

                // Encrypt session key with sender's public key
                val rsaCipherForSender = Cipher.getInstance("RSA/ECB/PKCS1Padding")
                rsaCipherForSender.init(Cipher.ENCRYPT_MODE, senderPublicKey)
                val encryptedSessionKeyForSender = rsaCipherForSender.doFinal(sessionKey.encoded)

                // Create JSON object for sending
                val jsonBody = JSONObject().apply {
                    put("chatId", chatId)
                    put("senderUsername", senderUsername)
                    put("receiverUsername", receiverUsername)
                    put("content", Base64.encodeToString(cipherText, Base64.DEFAULT))
                    put("receiver_encrypted_key", Base64.encodeToString(encryptedSessionKeyForReceiver, Base64.DEFAULT))
                    put("sender_encrypted_key", Base64.encodeToString(encryptedSessionKeyForSender, Base64.DEFAULT))
                    put("iv", Base64.encodeToString(iv, Base64.DEFAULT))
                }

                val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())

                // URL for sending message
                val sendMessageUrl = "https://rtusecretwhisper.ru/api/createMessage/send"

                // Create request for sending message
                val sendMessageRequest = Request.Builder()
                    .url(sendMessageUrl)
                    .post(requestBody)
                    .build()

                // Asynchronous request for sending message
                client.newCall(sendMessageRequest).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Log.e("SendMessage", "Failed to send message", e)
                    }

                    override fun onResponse(call: Call, response: Response) {
                        if (response.isSuccessful) {
                            Log.d("SendMessage", "Message sent successfully")
                        } else {
                            Log.e("SendMessage", "Failed to send message with response code: ${response.code}")
                        }
                    }
                })
            } else {
                Log.e("SendMessage", "Sender's public key not found in SharedPreferences")
            }
        }
    }
}


fun fetchPublicKey(context: Context, username: String, callback: (ByteArray?) -> Unit) {
    val client = OkHttpClient()

    val url = "https://rtusecretwhisper.ru/api/getPublicKey/$username"

    val request = Request.Builder()
        .url(url)
        .get()
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            Log.e("FetchPublicKey", "Failed to get public key", e)
            callback(null)
        }

        override fun onResponse(call: Call, response: Response) {
            if (response.isSuccessful) {
                response.body?.string()?.let { responseData ->
                    Log.d("FetchPublicKey", "Response data: $responseData") // Added output
                    val jsonObject = JSONObject(responseData)
                    if (jsonObject.has("public_key")) {
                        val publicKeyBase64 = jsonObject.getString("public_key")
                        val publicKey = if (publicKeyBase64.isNotEmpty()) {
                            Base64.decode(publicKeyBase64, Base64.DEFAULT)
                        } else {
                            byteArrayOf() // Empty array if key is empty
                        }
                        Log.d("FetchPublicKey", "Decoded public key: ${publicKey.joinToString()}") // Log decoded key
                        callback(publicKey)
                    } else {
                        Log.e("FetchPublicKey", "Public key not found in JSON response")
                        callback(null)
                    }
                }
            } else {
                Log.e("FetchPublicKey", "Failed to get public key with response code: ${response.code}")
                callback(null)
            }
        }
    })
}



data class Message(
    val senderId: Int,
    val receiverId: Int,
    val encryptedMessage: String,
    val senderEncryptedKey: String,
    val receiverEncryptedKey: String,
    val iv: String,
    val timestamp: String
)


fun decryptMessage(encryptedMessage: ByteArray, encryptedKey: ByteArray, iv: ByteArray, context: Context): String {
    val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    val sharedPreferences = EncryptedSharedPreferences.create(
        "secret_shared_prefs",
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    val privateKeyString = sharedPreferences.getString("private_key", null)
    val privateKey = privateKeyString?.let { keyString ->
        val privateKeyBytes = Base64.decode(keyString, Base64.DEFAULT)
        val keySpec = PKCS8EncodedKeySpec(privateKeyBytes)
        val keyFactory = KeyFactory.getInstance("RSA")
        keyFactory.generatePrivate(keySpec)
    }

    val rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
    rsaCipher.init(Cipher.DECRYPT_MODE, privateKey)
    val sessionKeyBytes = rsaCipher.doFinal(encryptedKey)
    val sessionKey = SecretKeySpec(sessionKeyBytes, 0, sessionKeyBytes.size, "AES")

    val cipher = Cipher.getInstance("AES/GCM/NoPadding")
    val gcmParameterSpec = GCMParameterSpec(128, iv)
    cipher.init(Cipher.DECRYPT_MODE, sessionKey, gcmParameterSpec)
    val plainTextBytes = cipher.doFinal(encryptedMessage)
    return String(plainTextBytes)
}


fun fetchMessages(context: Context, chatId: Int, callback: (List<Message>) -> Unit) {
    val client = OkHttpClient()
    val url = "https://rtusecretwhisper.ru/api/getMessages/$chatId"

    val request = Request.Builder()
        .url(url)
        .get()
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            Log.e("FetchMessages", "Failed to fetch messages", e)
        }

        override fun onResponse(call: Call, response: Response) {
            if (response.isSuccessful) {
                response.body?.string()?.let { responseData ->
                    try {
                        val jsonArray = JSONArray(responseData)
                        val messages = mutableListOf<Message>()
                        for (i in 0 until jsonArray.length()) {
                            val jsonObject = jsonArray.getJSONObject(i)
                            val message = Message(
                                senderId = jsonObject.getInt("sender_id"),
                                receiverId = jsonObject.getInt("receiver_id"),
                                encryptedMessage = jsonObject.getString("encrypted_message"),
                                senderEncryptedKey = jsonObject.getString("sender_encrypted_key"),
                                receiverEncryptedKey = jsonObject.getString("receiver_encrypted_key"),
                                iv = jsonObject.getString("iv"),
                                timestamp = jsonObject.getString("timestamp")
                            )
                            messages.add(message)
                        }
                        callback(messages)
                    } catch (e: JSONException) {
                        Log.e("FetchMessages", "Error parsing JSON response", e)
                    }
                }
            } else {
                Log.e("FetchMessages", "Failed to fetch messages with response code: ${response.code}")
            }
        }
    })
}
