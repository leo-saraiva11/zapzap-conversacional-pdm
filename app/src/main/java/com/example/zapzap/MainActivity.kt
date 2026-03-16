package com.example.zapzap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.zapzap.navigation.ZapZapNavGraph
import com.example.zapzap.ui.theme.ZapZapTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Activity principal do ZapZap.
 * Utiliza single-activity pattern com Jetpack Compose Navigation.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ZapZapTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ZapZapNavGraph()
                }
            }
        }
    }
}
