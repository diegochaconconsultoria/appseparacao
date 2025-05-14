package com.example.separadorpedidos.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1976D2),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFBBDEFB),
    onPrimaryContainer = Color(0xFF0D47A1),
    secondary = Color(0xFF26A69A),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFB2DFDB),
    onSecondaryContainer = Color(0xFF00695C),
    tertiary = Color(0xFFFF6F00),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFCC80),
    onTertiaryContainer = Color(0xFFE65100),
    error = Color(0xFFE57373),
    errorContainer = Color(0xFFFFEBEE),
    onError = Color.White,
    onErrorContainer = Color(0xFFD32F2F),
    background = Color(0xFFFAFAFA),
    onBackground = Color(0xFF212121),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF212121),
    surfaceVariant = Color(0xFFF5F5F5),
    onSurfaceVariant = Color(0xFF424242),
    outline = Color(0xFFBDBDBD),
    inverseSurface = Color(0xFF303030),
    inverseOnSurface = Color(0xFFF5F5F5),
    inversePrimary = Color(0xFF90CAF9),
    surfaceTint = Color(0xFF1976D2)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF90CAF9),
    onPrimary = Color(0xFF0D47A1),
    primaryContainer = Color(0xFF1565C0),
    onPrimaryContainer = Color(0xFFE3F2FD),
    secondary = Color(0xFF4DB6AC),
    onSecondary = Color(0xFF004D40),
    secondaryContainer = Color(0xFF00695C),
    onSecondaryContainer = Color(0xFFE0F2F1),
    tertiary = Color(0xFFFFB74D),
    onTertiary = Color(0xFFE65100),
    tertiaryContainer = Color(0xFFFF8F00),
    onTertiaryContainer = Color(0xFFFFF3E0),
    error = Color(0xFFFF8A80),
    errorContainer = Color(0xFFD32F2F),
    onError = Color(0xFFFFEBEE),
    onErrorContainer = Color(0xFFFFCDD2),
    background = Color(0xFF121212),
    onBackground = Color(0xFFE0E0E0),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE0E0E0),
    surfaceVariant = Color(0xFF424242),
    onSurfaceVariant = Color(0xFFBDBDBD),
    outline = Color(0xFF616161),
    inverseSurface = Color(0xFFE0E0E0),
    inverseOnSurface = Color(0xFF303030),
    inversePrimary = Color(0xFF1976D2),
    surfaceTint = Color(0xFF90CAF9)
)

@Composable
fun SeparadorPedidosTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}