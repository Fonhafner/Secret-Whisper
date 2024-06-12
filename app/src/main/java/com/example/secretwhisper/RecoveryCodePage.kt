import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.secretwhisper.LoginActivity

@Composable
fun RecoveryCodePage(navController: NavController, recoveryCode: String) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Код восстановления",
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Код поможет восстановить доступ к аккаунту при утере пароля",
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = recoveryCode,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Button(
            onClick = {
                // Переход на LoginActivity при нажатии на кнопку "Продолжить"
                navController.context.startActivity(Intent(navController.context, LoginActivity::class.java))
            }
        ) {
            Text(text = "Продолжить")
        }
    }
}
