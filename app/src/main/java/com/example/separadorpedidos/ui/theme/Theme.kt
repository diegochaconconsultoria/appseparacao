package com.example.separadorpedidos.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    // CORES VERMELHAS PRINCIPAIS
    primary = Color(0xFF9F2340),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDAD6),
    onPrimaryContainer = Color(0xFF410002),

    // CORES SECUNDÁRIAS (laranja/rosa complementar)
    secondary = Color(0xFFFF6B35),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDAD1),
    onSecondaryContainer = Color(0xFF441000),

    // CORES TERCIÁRIAS (rosa vibrante)
    tertiary = Color(0xFFD63384),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFD9E5),
    onTertiaryContainer = Color(0xFF390014),

    // CORES DE ERRO
    error = Color(0xFFBA1A1A),
    errorContainer = Color(0xFFFFDAD6),
    onError = Color.White,
    onErrorContainer = Color(0xFF410002),

    // CORES DE FUNDO
    background = Color(0xFFFFFBFF),
    onBackground = Color(0xFF201A1A),
    surface = Color(0xFFFFFBFF),
    onSurface = Color(0xFF201A1A),
    surfaceVariant = Color(0xFFF5DDDA),
    onSurfaceVariant = Color(0xFF534341),
    outline = Color(0xFF857370),
    inverseSurface = Color(0xFF362F2E),
    inverseOnSurface = Color(0xFFFBEEED),
    inversePrimary = Color(0xFFFFB4AB),
    surfaceTint = Color(0xFF9F2340)
)

private val DarkColorScheme = darkColorScheme(
    // CORES VERMELHAS PARA TEMA ESCURO
    primary = Color(0xFFFFB4AB),
    onPrimary = Color(0xFF690005),
    primaryContainer = Color(0xFF93000A),
    onPrimaryContainer = Color(0xFFFFDAD6),

    // CORES SECUNDÁRIAS
    secondary = Color(0xFFFFB59D),
    onSecondary = Color(0xFF5F1600),
    secondaryContainer = Color(0xFF7F2B00),
    onSecondaryContainer = Color(0xFFFFDAD1),

    // CORES TERCIÁRIAS
    tertiary = Color(0xFFFFB0CA),
    onTertiary = Color(0xFF541E2B),
    tertiaryContainer = Color(0xFF6E3441),
    onTertiaryContainer = Color(0xFFFFD9E5),

    // CORES DE ERRO
    error = Color(0xFFFFB4AB),
    errorContainer = Color(0xFF93000A),
    onError = Color(0xFF690005),
    onErrorContainer = Color(0xFFFFDAD6),

    // CORES DE FUNDO ESCURO
    background = Color(0xFF201A1A),
    onBackground = Color(0xFFEDE0DE),
    surface = Color(0xFF201A1A),
    onSurface = Color(0xFFEDE0DE),
    surfaceVariant = Color(0xFF534341),
    onSurfaceVariant = Color(0xFFD8C2BE),
    outline = Color(0xFFA08C89),
    inverseSurface = Color(0xFFEDE0DE),
    inverseOnSurface = Color(0xFF362F2E),
    inversePrimary = Color(0xFF9F2340),
    surfaceTint = Color(0xFFFFB4AB)
)

@Composable
fun SeparadorPedidosTheme(
    darkTheme: Boolean = false, // FORÇAR TEMA CLARO - mudança aqui
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme // SEMPRE usar tema claro

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}