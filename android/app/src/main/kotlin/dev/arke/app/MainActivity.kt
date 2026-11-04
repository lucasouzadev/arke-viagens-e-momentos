package dev.arke.app

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val places = ViewModelProvider(this)[PlaceIdeasViewModel::class.java]
        setContent {
            // Candidate from ADR-006; pending visual approval, not final identity.
            val brandBlue = Color(0xFF4C728A)
            val colors = if (isSystemInDarkTheme()) {
                darkColorScheme(primary = brandBlue, onPrimary = Color.White)
            } else {
                lightColorScheme(primary = brandBlue, onPrimary = Color.White)
            }
            MaterialTheme(colorScheme = colors, typography = ArkeTypography) {
                ArkeHome(places, captureDetails = BuildConfig.DEBUG && intent.getBooleanExtra("capturePlaceDetails", false),
                    captureEdit = BuildConfig.DEBUG && intent.getBooleanExtra("capturePlaceEdit", false),
                    journalCapture = if (BuildConfig.DEBUG) intent.getStringExtra("captureJournal") else null)
            }
        }
    }
}
