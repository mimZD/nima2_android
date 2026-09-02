package org.eshragh.nima2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import org.eshragh.nima2.ui.navigation.AppNavigation
import org.eshragh.nima2.ui.theme.Nima2Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Nima2Theme {
                AppNavigation()
            }
        }
    }
}