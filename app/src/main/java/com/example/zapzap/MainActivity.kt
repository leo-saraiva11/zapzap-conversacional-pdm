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
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        val permissionState = androidx.core.content.ContextCompat.checkSelfPermission(
                            this@MainActivity, android.Manifest.permission.POST_NOTIFICATIONS
                        )
                        if (permissionState != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                            val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
                                androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
                            ) {}
                            androidx.compose.runtime.LaunchedEffect(Unit) {
                                launcher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }
                    }
                    ZapZapNavGraph()
                }
            }
        }
    }
}
