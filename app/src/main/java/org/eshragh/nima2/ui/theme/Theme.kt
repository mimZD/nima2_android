package org.eshragh.nima2.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,               // #0066E6
    onPrimary = PureWhite,               // #FFFFFF
    primaryContainer = SoftCyanContainer,// #E0FBF7
    onPrimaryContainer = NavyText,       // #172554

    secondary = BrandCyan,               // #00D6C7
    onSecondary = NavyText,              // #172554
    secondaryContainer = LightCyan,      // #66E6DC
    onSecondaryContainer = PrimaryDarkBlue,// #003B8F

    tertiary = HighlightBlue,            // #00AEEF
    onTertiary = PureWhite,              // #FFFFFF
    tertiaryContainer = SoftCyanContainer,
    onTertiaryContainer = PrimaryDarkBlue,

    background = PureWhite,              // #FFFFFF
    onBackground = NavyText,             // #172554

    surface = PureWhite,                 // #FFFFFF
    onSurface = NavyText,                // #172554
    onSurfaceVariant = TextGrey,         // #6B7280

    surfaceContainerHigh = SoftSurfaceBg,
    surfaceContainerLow = PureWhite,

    error = Color(0xFFEF4444),
    onError = PureWhite,
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF991B1B)
)

private val DarkColorScheme = darkColorScheme(
    primary = HighlightBlue,             // #00AEEF
    onPrimary = NavyText,                // #172554
    primaryContainer = PrimaryDarkBlue,  // #003B8F
    onPrimaryContainer = PureWhite,

    secondary = BrandCyan,               // #00D6C7
    onSecondary = NavyText,
    secondaryContainer = DarkCyan,       // #009E95
    onSecondaryContainer = PureWhite,

    tertiary = LightCyan,
    onTertiary = NavyText,

    background = Color(0xFF0F172A),
    onBackground = PureWhite,

    surface = Color(0xFF1E293B),
    onSurface = PureWhite,
    onSurfaceVariant = Color(0xFF94A3B8),

    surfaceContainerHigh = Color(0xFF334155),
    surfaceContainerLow = Color(0xFF1E293B),

    error = Color(0xFFF87171),
    onError = NavyText,
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFECACA)
)

@Composable
fun Nima2Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Set dynamicColor = false by default so custom brand logo colors are ALWAYS preserved
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
