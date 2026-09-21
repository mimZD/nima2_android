package org.eshragh.nima2

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.activity.viewModels
import org.eshragh.nima2.ui.home.HomeViewModel
import org.eshragh.nima2.ui.navigation.AppNavigation
import org.eshragh.nima2.ui.theme.Nima2Theme
import org.eshragh.nima2.util.ShareManager
import java.util.Locale

class MainActivity : ComponentActivity() {
    
    // Globally shared ViewModel instance
    private val homeViewModel: HomeViewModel by viewModels {
        val app = applicationContext as org.eshragh.nima2.NimaApp
        val userPrefs = org.eshragh.nima2.data.pref.UserPreferencesRepository(applicationContext)
        val authRepo = org.eshragh.nima2.data.repository.AuthRepository(userPrefs)
        HomeViewModel.Factory(app.cardRepository, authRepo, userPrefs)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)

        // Force native RTL direction
        window.decorView.layoutDirection = View.LAYOUT_DIRECTION_RTL
        val config = resources.configuration
        config.setLayoutDirection(Locale("fa"))
        @Suppress("DEPRECATION")
        resources.updateConfiguration(config, resources.displayMetrics)

        enableEdgeToEdge()
        setContent {
            Nima2Theme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    AppNavigation(homeViewModel = homeViewModel)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return
        android.util.Log.d("NIMA2_SHARE", "MainActivity handleIntent Action: ${intent.action}, Type: ${intent.type}")
        
        val extractedUris = mutableListOf<Uri>()
        var extractedText: String? = intent.getStringExtra(Intent.EXTRA_TEXT) ?: intent.getStringExtra(Intent.EXTRA_SUBJECT)

        // Thorough extraction from ClipData
        intent.clipData?.let { clipData ->
            android.util.Log.d("NIMA2_SHARE", "Checking ClipData: count=${clipData.itemCount}")
            for (i in 0 until clipData.itemCount) {
                val item = clipData.getItemAt(i)
                item.uri?.let { 
                    android.util.Log.d("NIMA2_SHARE", "Extracted URI from ClipData: $it")
                    if (!extractedUris.contains(it)) extractedUris.add(it) 
                }
                if (extractedText == null && item.text != null) {
                    extractedText = item.text.toString()
                    android.util.Log.d("NIMA2_SHARE", "Extracted text from ClipData: $extractedText")
                }
            }
        }

        when (intent.action) {
            Intent.ACTION_SEND, Intent.ACTION_SEND_MULTIPLE -> {
                val streamUris = if (intent.action == Intent.ACTION_SEND) {
                    val uri = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
                    }
                    if (uri != null) listOf(uri) else null
                } else {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM, Uri::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)
                    }
                }
                
                streamUris?.forEach { 
                    android.util.Log.d("NIMA2_SHARE", "Extracted URI from EXTRA_STREAM: $it")
                    if (!extractedUris.contains(it)) extractedUris.add(it) 
                }
                
                if (extractedText != null || extractedUris.isNotEmpty()) {
                    android.util.Log.d("NIMA2_SHARE", "Posting share data to Manager. Text: ${extractedText?.take(10)}, Uris: ${extractedUris.size}")
                    ShareManager.setShareData(extractedText, if (extractedUris.isEmpty()) null else extractedUris)
                }
            }
        }
    }
}