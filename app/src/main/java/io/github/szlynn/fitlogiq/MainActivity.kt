package io.github.szlynn.fitlogiq

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import io.github.szlynn.fitlogiq.ui.navigation.FitLogIQNavHost
import io.github.szlynn.fitlogiq.ui.theme.FitLogIQTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FitLogIQTheme {
                FitLogIQNavHost()
            }
        }
    }
}
