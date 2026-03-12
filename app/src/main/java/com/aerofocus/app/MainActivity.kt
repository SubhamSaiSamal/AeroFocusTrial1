package com.aerofocus.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.aerofocus.app.ui.navigation.AeroFocusNavGraph
import com.aerofocus.app.ui.theme.AeroFocusTheme
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
            AeroFocusTheme {
                AeroFocusNavGraph()
            }
        }
    }
}
