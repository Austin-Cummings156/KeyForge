package com.example.keyforge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.room.Room
import com.example.keyforge.data.local.KeyForgeDatabase
import com.example.keyforge.data.repository.CredentialRepository
import com.example.keyforge.ui.CredentialViewModel
import com.example.keyforge.ui.CredentialViewModelFactory
import com.example.keyforge.ui.screens.AddCredentialScreen
import com.example.keyforge.ui.screens.CredentialListScreen
import com.example.keyforge.ui.theme.KeyForgeTheme

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: CredentialViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.statusBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        val database = Room.databaseBuilder(
            applicationContext,
            KeyForgeDatabase::class.java,
            "keyforge_database"
        ).build()

        val repository = CredentialRepository(database.credentialDao())

        val factory = CredentialViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[CredentialViewModel::class.java]

        setContent {
            val credentials = viewModel.credentials.collectAsStateWithLifecycle()

            var showAddScreen by remember { mutableStateOf(false) }

            KeyForgeTheme {
                if (showAddScreen) {
                    AddCredentialScreen(
                        onSave = {
                            viewModel.insertCredential(it)
                            showAddScreen = false
                        },
                    )
                } else {
                    CredentialListScreen(
                        credentials = credentials.value,
                        onAddClick = { showAddScreen = true },
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    KeyForgeTheme {
        Greeting("Android")
    }
}