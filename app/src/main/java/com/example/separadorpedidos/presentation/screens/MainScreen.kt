package com.example.separadorpedidos.presentation.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.separadorpedidos.data.model.SetorDisponivel
import com.example.separadorpedidos.presentation.viewmodel.MainViewModel
import com.example.separadorpedidos.ui.components.*
import com.example.separadorpedidos.ui.theme.SeparadorPedidosTheme
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onPedidoEncontrado: (String, String, List<SetorDisponivel>) -> Unit = { _, _, _ -> },
    viewModel: MainViewModel = viewModel()
) {
    var numeroPedido by remember { mutableStateOf("") }

    var voiceError by remember { mutableStateOf<String?>(null) }
    // Estado para mostrar o snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    // Efeito para mostrar snackbar em caso de erro
    LaunchedEffect(voiceError) {
        voiceError?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
            voiceError = null
        }
    }

    // Observar quando o pedido for encontrado
    LaunchedEffect(uiState.pedidoEncontrado) {
        if (uiState.pedidoEncontrado) {
            onPedidoEncontrado(
                uiState.numeroPedido,
                uiState.nomeCliente,
                uiState.setoresDisponiveis
            )
            viewModel.limparState()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        // App Bar moderno
        TopAppBar(
            title = {
                Text(
                    "MVK Separação",
                    fontWeight = FontWeight.Bold
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary
            )
        )

        AnimatedScreen {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Card principal
                AnimatedCard(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Ícone e título
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Receipt,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Buscar Pedido de Venda",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Campo de busca moderno
                        ModernSearchField(
                            value = numeroPedido,
                            onValueChange = {
                                // Aceitar apenas dígitos
                                if (it.all { char -> char.isDigit() } || it.isEmpty()) {
                                    numeroPedido = it
                                }
                            },
                            placeholder = "Digite o número do pedido",
                            enabled = !uiState.isLoading,
                            keyboardType = KeyboardType.Number,
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            trailingIcon = {
                                // Aqui adicionamos o botão de reconhecimento de voz
                                VoiceSearchButton(
                                    onVoiceResult = { result ->
                                        numeroPedido = result
                                        // Opcionalmente, buscar automaticamente após reconhecimento
                                        viewModel.buscarPedido(result)
                                    },
                                    onError = { error ->
                                        voiceError = error
                                    }
                                )
                            }
                        )

                        Box(modifier = Modifier.fillMaxSize()) {
                            // Conteúdo existente...

                            SnackbarHost(
                                hostState = snackbarHostState,
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(16.dp)
                            )
                        }

                        // Botão de busca moderno
                        ModernButton(
                            onClick = { viewModel.buscarPedido(numeroPedido) },
                            enabled = numeroPedido.isNotBlank(),
                            loading = uiState.isLoading
                        ) {
                            if (uiState.isLoading) {
                                Text("Buscando...")
                            } else {
                                Text("Buscar Pedido")
                            }
                        }
                    }
                }

                // Card de erro (se houver) - CORRIGIDO
                uiState.error?.let { error ->
                    AnimatedCard(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Erro",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = error,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                // Card informativo
                AnimatedCard(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "💡 Dica",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Digite o número do pedido para visualizar os setores disponíveis para separação de materiais.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    SeparadorPedidosTheme {
        MainScreen()
    }
}