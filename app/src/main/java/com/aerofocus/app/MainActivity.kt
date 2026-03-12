package com.aerofocus.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single-activity host for the entire Compose UI.
 *
 * Edge-to-edge rendering is enabled so the dark theme extends
 * behind the system bars for full immersion.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            // Theme and NavGraph will be wired in Phase 6.
            // For now, a placeholder Surface confirms the build compiles.
            Surface(modifier = Modifier.fillMaxSize()) {
                // TODO: AeroFocusTheme { AeroFocusNavGraph() }
            }
        }
    }
}
