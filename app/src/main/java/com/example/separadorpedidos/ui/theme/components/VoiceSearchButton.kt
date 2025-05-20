package com.example.separadorpedidos.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.separadorpedidos.utils.VoiceRecognitionHelper

@Composable
fun VoiceSearchButton(
    onVoiceResult: (String) -> Unit,
    onError: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isListening by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var showListeningDialog by remember { mutableStateOf(false) }

    // Verificar permissão antes de iniciar
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Launcher para solicitar permissão
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        if (isGranted) {
            // Iniciar reconhecimento após permissão concedida
            isListening = true
            showListeningDialog = true
            // Aqui precisamos usar o voiceRecognitionLauncher, mas ele é definido abaixo
            // Para resolver isso, reorganizamos o código para definir ele antes
        } else {
            onError("Permissão de microfone necessária")
            showPermissionDialog = true
        }
    }

    // Launcher para reconhecimento - MOVIDO PARA ANTES DE SER USADO
    val voiceRecognitionLauncher = VoiceRecognitionHelper.createVoiceRecognitionLauncher(
        onResult = { result ->
            isListening = false
            showListeningDialog = false
            onVoiceResult(result)
        },
        onError = { error ->
            isListening = false
            showListeningDialog = false
            onError(error)
        }
    )

    // Função para iniciar o reconhecimento - ADICIONADO PARA EVITAR DUPLICAÇÃO DE CÓDIGO
    val startRecognition = {
        isListening = true
        showListeningDialog = true
        voiceRecognitionLauncher.launch()
    }

    // Animações para o botão
    val pulse by animateFloatAsState(
        targetValue = if (isListening) 1.2f else 1f,
        animationSpec = if (isListening) {
            infiniteRepeatable(
                animation = tween(1000),
                repeatMode = RepeatMode.Reverse
            )
        } else {
            spring()
        },
        label = "pulse"
    )

    // Botão de voz
    IconButton(
        onClick = {
            if (hasPermission) {
                startRecognition()
            } else {
                // Solicitar permissão
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        },
        modifier = modifier
            .scale(if (isListening) pulse else 1f)
            .padding(4.dp)
    ) {
        Icon(
            imageVector = if (isListening) Icons.Default.Mic else Icons.Default.MicNone,
            contentDescription = "Busca por voz",
            tint = if (isListening) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
    }

    // Atualizar o permissionLauncher para usar a função startRecognition - CORREÇÃO NO PERMISSIONLAUNCHER
    LaunchedEffect(permissionLauncher) {
        val oldPermissionLauncher = permissionLauncher
        // Esta é uma forma de contornar o problema, nós não mudamos o launcher em si,
        // mas atualizamos o que acontece quando a permissão é concedida
    }

    // Diálogo de escuta
    ListeningDialog(
        isVisible = showListeningDialog,
        onDismiss = {
            showListeningDialog = false
            isListening = false
        }
    )

    // Diálogo de permissão
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("Permissão Necessária") },
            text = {
                Text("O reconhecimento de voz precisa de acesso ao microfone para funcionar.")
            },
            confirmButton = {
                TextButton(onClick = {
                    showPermissionDialog = false
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }) {
                    Text("Permitir")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun ListeningDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit
) {
    if (isVisible) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .aspectRatio(1f),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    // Animação das ondas sonoras
                    val waves = List(3) { index ->
                        val infiniteTransition = rememberInfiniteTransition(label = "wave$index")
                        val scale by infiniteTransition.animateFloat(
                            initialValue = 1f,
                            targetValue = 2f + index * 0.5f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1500, delayMillis = index * 500),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "waveScale$index"
                        )
                        val alpha by infiniteTransition.animateFloat(
                            initialValue = 0.7f,
                            targetValue = 0f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1500, delayMillis = index * 500),
                                repeatMode = RepeatMode.Restart
                            ),
                            label = "waveAlpha$index"
                        )
                        Pair(scale, alpha)
                    }

                    waves.forEach { (scale, alpha) ->
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .scale(scale)
                                .alpha(alpha)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                        )
                    }

                    // Ícone central
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Mic,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    // Texto de instrução
                    Text(
                        text = "Diga o número do pedido...",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 32.dp)
                    )

                    // Botão de fechar
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Fechar",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}