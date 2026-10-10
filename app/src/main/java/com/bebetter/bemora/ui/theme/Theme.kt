package com.bebetter.bemora.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFF81D5C4),
    secondary = androidx.compose.ui.graphics.Color(0xFFB8CBC5),
    tertiary = androidx.compose.ui.graphics.Color(0xFFE5C18D),
    background = androidx.compose.ui.graphics.Color(0xFF101817),
    surface = androidx.compose.ui.graphics.Color(0xFF101817),
    surfaceVariant = androidx.compose.ui.graphics.Color(0xFF26332F)
)

private val LightColorScheme = lightColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFF176B5B),
    secondary = androidx.compose.ui.graphics.Color(0xFF50665E),
    tertiary = androidx.compose.ui.graphics.Color(0xFF805A27),
    background = androidx.compose.ui.graphics.Color(0xFFF7FAF6),
    surface = androidx.compose.ui.graphics.Color(0xFFF7FAF6),
    surfaceVariant = androidx.compose.ui.graphics.Color(0xFFE1EBE4)

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun BeMoraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
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