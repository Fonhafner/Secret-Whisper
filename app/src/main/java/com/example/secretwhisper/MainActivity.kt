package com.example.secretwhisper


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.secretwhisper.TokenManager.clearToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppContent()
        }
    }
    @Composable
    fun AppContent() {
        var currentScreen by remember { mutableStateOf(Screen.Chats) }

        val chatsState = remember { mutableStateOf<List<Chat>?>(null) }
        val contactsState = remember { mutableStateOf<List<String>?>(null) }

        val context = LocalContext.current // Получаем контекст

        // Загрузка чатов и обновление состояния
        LaunchedEffect(Unit) {
            val loggedInUsername = getLoggedInUsername(context)
            if (loggedInUsername != null) {
                loadChatsAndUpdateState(loggedInUsername, chatsState)
            } else {
                Log.e("AppContent", "Error: Logged in username is null")
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    backgroundColor = Color.White, // Устанавливаем белый цвет для топбара
                    title = {
                        Text(
                            text = when (currentScreen) {
                                Screen.Contacts -> "Контакты"
                                Screen.Chats -> "Чаты"
                                Screen.Management -> "Управление"
                            },
                            color = Color.Black // Устанавливаем черный цвет для текста в топбаре
                        )
                    },
                    modifier = Modifier
                        .height(90.dp) // Увеличиваем высоту топбара до 90dp
                )
            },
            content = {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    when (currentScreen) {
                        Screen.Contacts -> {
                            val (searchQuery, setSearchQuery) = remember { mutableStateOf("") }
                            SearchField(searchQuery = searchQuery) { setSearchQuery(it) }
                            ContactList(searchQuery = searchQuery)
                        }
                        Screen.Chats -> {
                            val (searchQuery, setSearchQuery) = remember { mutableStateOf("") }
                            SearchField(searchQuery = searchQuery) { setSearchQuery(it) }
                            ChatList(chatsState.value, searchQuery) { chat ->
                                Log.d("ChatList", "Selected chat_id: ${chat.chat_id}, receiver_username: ${chat.receiver_username}")
                                val intent = Intent(context, ChatActivity::class.java).apply {
                                    putExtra("chatId", chat.chat_id)
                                    putExtra("username", chat.receiver_username)
                                }
                                context.startActivity(intent)
                            }
                        }
                        Screen.Management -> {
                            val loggedInUsername = getLoggedInUsername(context)
                            ManagementTabs(loggedInUsername)
                        }
                    }
                }
            },
            bottomBar = {
                BottomNavigation(currentScreen) { screen -> currentScreen = screen }
            }
        )
    }







    private fun loadChatsAndUpdateState(username: String, chatsState: MutableState<List<Chat>?>) {
        CoroutineScope(Dispatchers.IO).launch {
            val loadedChats = try {
                loadChats(username)
            } catch (e: Exception) {
                Log.e("MainActivity", "Error loading chats", e)
                emptyList()
            }
            withContext(Dispatchers.Main) {
                chatsState.value = loadedChats
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

                // Extract username from JSON
                val jsonObject = JSONObject(decodedString)
                loggedInUsername = jsonObject.optString("username")
            }
        }
        Log.d("ProfileActivity", "Logged in username from SharedPreferences: $loggedInUsername")
        return loggedInUsername
    }

    @Composable
    fun SearchField(searchQuery: String, onSearchQueryChange: (String) -> Unit) {
        val cornerSize = 4.dp
        val searchFieldBackgroundColor = Color(0xFFF7F7FC)

        Box(
            modifier = Modifier
                .width(410.dp)
                .height(55.dp)
                .background(color = searchFieldBackgroundColor, shape = RoundedCornerShape(cornerSize))
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            TextField(
                value = searchQuery,
                onValueChange = { onSearchQueryChange(it) },
                placeholder = { Text("Поиск") },
                modifier = Modifier.fillMaxSize(),
                textStyle = LocalTextStyle.current.copy(color = Color.Black), // Устанавливаем черный цвет для текста
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.Transparent, // Устанавливаем прозрачный цвет для фона текстового поля
                    focusedIndicatorColor = Color.Transparent, // Устанавливаем прозрачный цвет для индикатора при фокусе
                    unfocusedIndicatorColor = Color.Transparent // Устанавливаем прозрачный цвет для индикатора без фокуса
                ),
                singleLine = true // Ограничиваем строку поиска одной строкой
            )
        }
    }


    @Composable
    fun ContactList(searchQuery: String) {
        var contacts by remember { mutableStateOf<List<String>?>(null) }
        val context = LocalContext.current
        val certificateRawResource = R.raw.sertifi  // Замените `your_certificate` на ваш файл сертификата

        fun loadContacts(query: String) {
            val client = createOkHttpClient(context, certificateRawResource)
            val retrofit = Retrofit.Builder()
                .baseUrl("https://rtusecretwhisper.ru/api/")  // Замените на ваш базовый URL
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val apiService = retrofit.create(ApiService::class.java)

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = apiService.searchContacts(query)
                    withContext(Dispatchers.Main) {
                        contacts = response
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        // Загружаем контакты при изменении поискового запроса
        LaunchedEffect(searchQuery) {
            if (searchQuery.isNotBlank()) {
                loadContacts(searchQuery)
            }
        }

        // Выводим список контактов, если они загружены
        contacts?.let { contactList ->
            if (contactList.isEmpty()) {
                Text(
                    text = "Нет результатов",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            } else {
                LazyColumn {
                    itemsIndexed(contactList) { index, contact ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)                                .clickable {
                                    // При нажатии на пользователя запускаем ProfileActivity и передаем информацию о пользователе
                                    val intent = Intent(context, ProfileActivity::class.java).apply {
                                        putExtra("username", contact)
                                    }
                                    context.startActivity(intent)
                                }
                        ) {
                            Text(
                                text = contact,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        } ?: run {
            // Если контакты еще загружаются, отображаем сообщение о загрузке
            Text(
                text = "Загрузка...",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }
    }

    @Composable
    fun ChatList(chats: List<Chat>?, searchQuery: String, onChatClicked: (Chat) -> Unit) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            chats?.let {
                LazyColumn {
                    itemsIndexed(chats) { index, chat ->
                        // Отфильтровываем чаты по поисковому запросу
                        if (chat.receiver_username.contains(searchQuery, ignoreCase = true)) {
                            ChatItem(chat = chat, onChatClicked = onChatClicked)
                            if (index < chats.size - 1) {
                                Divider() // Добавляем разделитель между чатами
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun ChatItem(chat: Chat, onChatClicked: (Chat) -> Unit) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clickable { onChatClicked(chat) },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${chat.receiver_username}",
                    fontWeight = FontWeight.Bold // Жирный шрифт для имени собеседника
                )
            }
            Text(
                text = formatTimestamp(chat.last_message_timestamp),
                style = MaterialTheme.typography.body2, // Используем стиль текста Material Design для времени сообщения
                textAlign = TextAlign.End,
                modifier = Modifier.padding(start = 8.dp) // Добавляем небольшой отступ от имени собеседника
            )
        }
    }

    fun formatTimestamp(timestamp: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("ru", "RU"))
        val currentDate = Calendar.getInstance()
        val messageDate = Calendar.getInstance()

        val date: Date = inputFormat.parse(timestamp) ?: Date()
        messageDate.time = date

        return when {
            isSameDay(currentDate, messageDate) -> {
                outputFormat.format(date)
            }
            isSameYear(currentDate, messageDate) -> {
                SimpleDateFormat("dd MMM, HH:mm", Locale("ru", "RU")).format(date)
            }
            else -> {
                SimpleDateFormat("dd.MM.yy", Locale("ru", "RU")).format(date)
            }
        }
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun isSameYear(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
    }

    interface ChatApiService {
        @GET("chats")
        suspend fun getChats(@Query("username") username: String): List<Chat>
    }

    data class Chat(
        val chat_id: Int,
        val receiver_username: String,
        val last_message_timestamp: String // Предположим, что время представлено в строковом формате
    )

    private suspend fun loadChats(username: String): List<Chat> {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://rtusecretwhisper.ru/api/getChats/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ChatApiService::class.java)

        return try {
            apiService.getChats(username)
        } catch (e: Exception) {
            // Записываем ошибку в лог
            Log.e("loadChats", "Ошибка загрузки чатов", e)
            emptyList() // Возвращаем пустой список в случае ошибки
        }
    }

    @Composable
    fun ManagementTabs(username: String?) {
        val context = LocalContext.current

        val tabs = listOf(
            "Логин пользователя",
            "Внешний вид",
            "Уведомления",
            "Приватность",
            "Использование памяти",
            "Помощь",
            "Выйти из аккаунта"
        )

        LazyColumn {
            itemsIndexed(tabs) { index, tab ->
                // Handle nullability of username
                if (tab == "Логин пользователя") {
                    // Отображаем ячейку для логина пользователя
                    LoginUserItem(username)
                } else if (tab == "Выйти из аккаунта") {
                    // Отображаем ячейку для выхода из аккаунта
                    LogoutItem()
                } else {
                    // Отображаем остальные ячейки
                    Text(
                        text = tab,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { /* Здесь нужно добавить логику для перехода на соответствующий экран */ }
                            .padding(16.dp)
                    )
                }
            }
        }
    }

    @Composable
    fun LoginUserItem(username: String?) {
        // Отображаем ячейку для логина пользователя
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(72.dp), // Увеличиваем высоту ячейки
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = username ?: "Unknown", // По умолчанию "Unknown", если username равен null
                fontWeight = FontWeight.Bold, // Жирный шрифт для логина
                modifier = Modifier.weight(1f) // Занимаем все доступное пространство
            )
        }
    }



    @Composable
    fun LogoutItem() {
        val context = LocalContext.current
        val logoutText = "Выйти из аккаунта"
        Text(
            text = logoutText,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    clearToken(context)
                    val intent = Intent(context, WelcomeActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    context.startActivity(intent)
                    (context as? ComponentActivity)?.finish()
                }
                .padding(16.dp)
        )
    }


    @Composable
    fun BottomNavigation(currentScreen: Screen, onScreenSelected: (Screen) -> Unit) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Button(onClick = { onScreenSelected(Screen.Contacts) }) {
                Text(text = "Контакты")
            }
            Button(onClick = { onScreenSelected(Screen.Chats) }) {
                Text(text = "Чаты")
            }
            Button(onClick = { onScreenSelected(Screen.Management) }) {
                Text(text = "Управление")
            }
        }
    }

    interface ApiService {
        @GET("search/contacts/")
        suspend fun searchContacts(@Query("query") query: String): List<String>
    }

    enum class Screen {
        Contacts, Chats, Management
    }
}
