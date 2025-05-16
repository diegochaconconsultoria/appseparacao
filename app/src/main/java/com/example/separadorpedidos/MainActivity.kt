package com.example.separadorpedidos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.example.separadorpedidos.navigation.AppNavigation
import com.example.separadorpedidos.ui.theme.SeparadorPedidosTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mudar para tema normal após splash
        setTheme(R.style.Theme_SeparadorPedidos)

        // Habilitar edge-to-edge para um visual mais moderno
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            SeparadorPedidosTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}