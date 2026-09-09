package org.eshragh.nima2

import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import org.eshragh.nima2.ui.navigation.AppNavigation
import org.eshragh.nima2.ui.theme.Nima2Theme
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Force native RTL direction on Window and Resources so all Android Dialogs inherit RTL natively
        window.decorView.layoutDirection = View.LAYOUT_DIRECTION_RTL
        val config = resources.configuration
        config.setLayoutDirection(Locale("fa"))
        @Suppress("DEPRECATION")
        resources.updateConfiguration(config, resources.displayMetrics)

        enableEdgeToEdge()
        setContent {
            Nima2Theme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    AppNavigation()
                }
            }
        }
    }
}